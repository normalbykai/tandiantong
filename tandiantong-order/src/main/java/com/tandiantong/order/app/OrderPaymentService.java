package com.tandiantong.order.app;

import com.tandiantong.catalog.product.CatalogInventoryService;
import com.tandiantong.catalog.product.ProductSkuProfile;
import com.tandiantong.catalog.tenant.TenantStoreScope;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.integration.wechatpay.WechatPayClient;
import com.tandiantong.integration.wechatpay.WechatPrepayResult;
import com.tandiantong.order.domain.OrderItemSnapshot;
import com.tandiantong.order.domain.OrderStatus;
import com.tandiantong.order.domain.PaymentRecord;
import com.tandiantong.order.domain.RefundRecord;
import com.tandiantong.order.domain.RefundStatus;
import com.tandiantong.order.domain.SalesOrder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class OrderPaymentService {

    private final CatalogInventoryService catalogInventoryService;
    private final WechatPayClient wechatPayClient;
    private final AtomicLong idSequence = new AtomicLong(1000);
    private final Map<String, SalesOrder> ordersByNo = new LinkedHashMap<>();
    private final Map<String, OrderCreationResult> createResultsByIdempotencyKey = new LinkedHashMap<>();
    private final Map<String, List<OrderItemSnapshot>> itemsByOrderNo = new LinkedHashMap<>();
    private final Map<String, PaymentRecord> paymentsByOrderNo = new LinkedHashMap<>();
    private final Map<String, RefundRecord> refundsByIdempotencyKey = new LinkedHashMap<>();

    public OrderPaymentService(CatalogInventoryService catalogInventoryService, WechatPayClient wechatPayClient) {
        this.catalogInventoryService = catalogInventoryService;
        this.wechatPayClient = wechatPayClient;
    }

    public OrderCreationResult createOrder(TenantStoreScope scope, CreateOrderCommand command) {
        String createKey = scope.tenantId() + ":" + command.idempotencyKey();
        if (createResultsByIdempotencyKey.containsKey(createKey)) {
            return createResultsByIdempotencyKey.get(createKey);
        }
        validateCreateCommand(command);
        String orderNo = "SO" + scope.tenantId() + idSequence.incrementAndGet();
        List<OrderItemSnapshot> snapshots = new ArrayList<>();
        int payAmountCent = 0;
        for (OrderSkuSelection selection : command.selections()) {
            ProductSkuProfile sku = catalogInventoryService.findSku(scope, selection.skuId());
            int addonAmountCent = selection.addonNames().stream()
                    .mapToInt(this::addonPriceCent)
                    .sum();
            int subtotal = (sku.priceCent() + addonAmountCent) * selection.quantity();
            payAmountCent += subtotal;
            snapshots.add(new OrderItemSnapshot(idSequence.incrementAndGet(), orderNo, sku.skuId(),
                    "桂花拿铁", sku.specificationText(), List.copyOf(selection.addonNames()),
                    sku.priceCent(), addonAmountCent, selection.quantity(), subtotal));
        }
        SalesOrder order = new SalesOrder(idSequence.incrementAndGet(), scope.tenantId(), scope.storeId(),
                null, orderNo, OrderStatus.PENDING_PAYMENT, payAmountCent, command.contactMobile(),
                command.pickupTimeText(), null, Instant.now(), null);
        WechatPrepayResult prepay = wechatPayClient.createPrepay(orderNo, payAmountCent, "摊点通商品订单");
        order = new SalesOrder(order.orderId(), order.tenantId(), order.storeId(), order.customerId(),
                order.orderNo(), order.status(), order.payAmountCent(), order.contactMobile(),
                order.pickupTimeText(), prepay.prepayId(), order.createdAt(), order.paidAt());
        for (OrderItemSnapshot item : snapshots) {
            catalogInventoryService.lockInventory(scope, item.skuId(), item.quantity(), orderNo);
        }
        ordersByNo.put(orderNo, order);
        itemsByOrderNo.put(orderNo, List.copyOf(snapshots));
        OrderCreationResult result = new OrderCreationResult(order, List.copyOf(snapshots), prepay);
        createResultsByIdempotencyKey.put(createKey, result);
        return result;
    }

    public SalesOrder findOrder(TenantStoreScope scope, String orderNo) {
        SalesOrder order = ordersByNo.get(orderNo);
        ensureOrderBelongsToScope(scope, order);
        return order;
    }

    public void handlePaymentCallback(String orderNo, String transactionId, int amountCent, String signature) {
        SalesOrder order = ordersByNo.get(orderNo);
        if (order == null) {
            throw businessError("支付回调订单不存在");
        }
        if (!wechatPayClient.verifyCallback(orderNo, transactionId, amountCent, signature)) {
            throw businessError("微信支付回调验签失败");
        }
        if (paymentsByOrderNo.containsKey(orderNo)) {
            return;
        }
        if (order.status() != OrderStatus.PENDING_PAYMENT) {
            throw businessError("当前订单状态不能确认支付");
        }
        if (order.payAmountCent() != amountCent) {
            throw businessError("支付金额与订单金额不一致");
        }
        TenantStoreScope scope = new TenantStoreScope(order.tenantId(), order.storeId(), 0L);
        for (OrderItemSnapshot item : itemsByOrderNo.get(orderNo)) {
            catalogInventoryService.confirmPaymentDeduct(scope, item.skuId(), item.quantity(), "PAY-" + transactionId);
        }
        paymentsByOrderNo.put(orderNo, new PaymentRecord(idSequence.incrementAndGet(), orderNo,
                transactionId, amountCent, Instant.now()));
        ordersByNo.put(orderNo, order.paid(transactionId));
    }

    public void cancelPendingOrder(TenantStoreScope scope, String orderNo, String reason) {
        SalesOrder order = findOrder(scope, orderNo);
        if (order.status() != OrderStatus.PENDING_PAYMENT) {
            throw businessError("只有待支付订单可以取消");
        }
        for (OrderItemSnapshot item : itemsByOrderNo.get(orderNo)) {
            catalogInventoryService.releaseLockedInventory(scope, item.skuId(), item.quantity(), orderNo);
        }
        ordersByNo.put(orderNo, order.withStatus(OrderStatus.CANCELED));
    }

    public RefundRecord refundWholeOrder(TenantStoreScope scope, String orderNo, String reason, String idempotencyKey) {
        String refundKey = scope.tenantId() + ":" + idempotencyKey;
        if (refundsByIdempotencyKey.containsKey(refundKey)) {
            return refundsByIdempotencyKey.get(refundKey);
        }
        SalesOrder order = findOrder(scope, orderNo);
        if (order.status() == OrderStatus.COMPLETED) {
            throw businessError("已完成订单不能在线退款");
        }
        if (order.status() != OrderStatus.PENDING_VERIFY) {
            throw businessError("只有待核销订单可以发起整单退款");
        }
        String refundNo = "RF" + scope.tenantId() + idSequence.incrementAndGet();
        ordersByNo.put(orderNo, order.withStatus(OrderStatus.REFUNDING));
        var refundResult = wechatPayClient.refund(orderNo, refundNo, order.payAmountCent(), reason);
        RefundRecord refund = new RefundRecord(idSequence.incrementAndGet(), orderNo, refundResult.refundNo(),
                order.payAmountCent(), refundResult.success() ? RefundStatus.SUCCESS : RefundStatus.FAILED,
                reason, Instant.now());
        if (refund.status() == RefundStatus.SUCCESS) {
            for (OrderItemSnapshot item : itemsByOrderNo.get(orderNo)) {
                catalogInventoryService.restoreRefundedInventory(scope, item.skuId(), item.quantity(), refundNo);
            }
            ordersByNo.put(orderNo, order.withStatus(OrderStatus.REFUNDED));
        }
        refundsByIdempotencyKey.put(refundKey, refund);
        return refund;
    }

    public void markCompletedForVerification(TenantStoreScope scope, String orderNo) {
        SalesOrder order = findOrder(scope, orderNo);
        ordersByNo.put(orderNo, order.withStatus(OrderStatus.COMPLETED));
    }

    private void validateCreateCommand(CreateOrderCommand command) {
        if (command.selections() == null || command.selections().isEmpty()) {
            throw businessError("订单至少需要一个商品");
        }
        for (OrderSkuSelection selection : command.selections()) {
            if (selection.quantity() <= 0) {
                throw businessError("商品数量必须大于零");
            }
        }
    }

    private int addonPriceCent(String addonName) {
        return switch (addonName) {
            case "燕麦奶" -> 300;
            case "浓缩咖啡" -> 400;
            default -> throw businessError("加料项不存在或已停用");
        };
    }

    private void ensureOrderBelongsToScope(TenantStoreScope scope, SalesOrder order) {
        if (order == null || !order.tenantId().equals(scope.tenantId()) || !order.storeId().equals(scope.storeId())) {
            throw businessError("订单资源不属于当前租户或门店");
        }
    }

    private BusinessException businessError(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
