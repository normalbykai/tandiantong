package com.tandiantong.order.app;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tandiantong.catalog.inventory.InventoryApplicationService;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.integration.wechatpay.WechatPayClient;
import com.tandiantong.integration.wechatpay.WechatPrepayResult;
import com.tandiantong.order.entity.BusinessIdempotencyRecordEntity;
import com.tandiantong.order.entity.PaymentRecordEntity;
import com.tandiantong.order.entity.RefundRecordEntity;
import com.tandiantong.order.entity.SalesOrderEntity;
import com.tandiantong.order.entity.SalesOrderItemEntity;
import com.tandiantong.order.mapper.BusinessIdempotencyRecordMapper;
import com.tandiantong.order.mapper.PaymentRecordMapper;
import com.tandiantong.order.mapper.RefundRecordMapper;
import com.tandiantong.order.mapper.SalesOrderItemMapper;
import com.tandiantong.order.mapper.SalesOrderMapper;
import com.tandiantong.security.tenant.MerchantSceneService;
import com.tandiantong.security.tenant.MerchantSceneService.MerchantSceneScope;
import com.tandiantong.verification.app.VerificationPersistenceService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品订单应用服务，负责下单、支付确认、整单退款和交易幂等编排。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class PersistentOrderService {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final String ORDER_DESCRIPTION = "摊点通商品订单";
    private static final String EMPTY_ADDON_SNAPSHOT = "";
    private static final int ZERO_ADDON_AMOUNT_CENT = 0;

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final BusinessIdempotencyRecordMapper idempotencyRecordMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final RefundRecordMapper refundRecordMapper;
    private final InventoryApplicationService inventoryApplicationService;
    private final MerchantSceneService merchantSceneService;
    private final WechatPayClient wechatPayClient;
    private final VerificationPersistenceService verificationPersistenceService;

    /**
     * 创建商品订单并锁定库存，同一租户下相同幂等键返回首次创建结果。
     */
    @Transactional
    public PersistentOrderResult createOrder(String sceneKey, PersistentCreateOrderCommand command) {
        MerchantSceneScope scope = merchantSceneService.resolveEnabledScene(sceneKey);
        validate(command);
        BusinessIdempotencyRecordEntity previous = findIdempotency(
                scope.tenantId(), BusinessType.ORDER_CREATE, command.idempotencyKey());
        if (previous != null) {
            return findCreatedOrder(scope.tenantId(), previous.getBusinessNo());
        }

        String orderNo = "SO" + scope.tenantId() + randomPart(16);
        List<OrderLine> lines = command.lines().stream()
                .map(line -> lockAndPrice(scope, line, orderNo))
                .toList();
        int amountCent = lines.stream().mapToInt(OrderLine::subtotalCent).sum();
        WechatPrepayResult prepay = wechatPayClient.createPrepay(orderNo, amountCent, ORDER_DESCRIPTION);
        insertOrder(scope, orderNo, amountCent, command.contactMobile(), command.pickupTimeText(), prepay.prepayId());
        lines.forEach(line -> insertOrderItem(scope, orderNo, line));
        insertIdempotency(scope.tenantId(), command.idempotencyKey(), BusinessType.ORDER_CREATE,
                orderNo, PersistenceResult.SUCCESS.code());
        return new PersistentOrderResult(orderNo, amountCent, OrderStatus.PENDING_PAYMENT.code(),
                prepay.prepayId(), prepay.payNonce(), null, null);
    }

    /**
     * 处理微信支付成功回调，使用订单号反查可信租户并保证状态条件更新最多成功一次。
     */
    @Transactional
    public PersistentOrderResult confirmPayment(String orderNo, String transactionId,
                                                int amountCent, String signature) {
        SalesOrderEntity order = findOrderForCallback(orderNo);
        if (!wechatPayClient.verifyCallback(orderNo, transactionId, amountCent, signature)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "微信支付回调验签失败");
        }
        if (OrderStatus.PENDING_VERIFY.matches(order.getStatus())) {
            return toResult(order);
        }
        if (!OrderStatus.PENDING_PAYMENT.matches(order.getStatus()) || order.getPayAmountCent() != amountCent) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "订单支付状态或金额不正确");
        }
        int updated = salesOrderMapper.updatePaidStatus(order.getTenantId(), orderNo,
                OrderStatus.PENDING_PAYMENT.code(), OrderStatus.PENDING_VERIFY.code(),
                LocalDateTime.now(BUSINESS_ZONE_ID));
        if (updated == 0) {
            return findCreatedOrder(order.getTenantId(), orderNo);
        }
        insertPaymentRecord(order, transactionId, amountCent);
        for (OrderLine line : orderLines(order)) {
            inventoryApplicationService.confirmPayment(order.getTenantId(), order.getStoreId(),
                    line.skuId(), line.quantity(), orderNo);
        }
        VerificationPersistenceService.Credential credential = verificationPersistenceService
                .issueOrderCredential(order.getTenantId(), order.getStoreId(), orderNo, "商品订单 " + orderNo);
        return new PersistentOrderResult(orderNo, amountCent, OrderStatus.PENDING_VERIFY.code(),
                order.getPrepayId(), "", credential.pickupNo(), credential.token());
    }

    /**
     * 对待核销订单发起整单退款，同一幂等键不会重复调用外部退款。
     */
    @Transactional
    public RefundResult refund(Long tenantId, Long storeId, String orderNo,
                               String idempotencyKey, String reason) {
        BusinessIdempotencyRecordEntity previous = findIdempotency(
                tenantId, BusinessType.ORDER_REFUND, idempotencyKey);
        if (previous != null) {
            RefundRecordEntity refund = refundRecordMapper.selectOne(Wrappers.<RefundRecordEntity>lambdaQuery()
                    .eq(RefundRecordEntity::getTenantId, tenantId)
                    .eq(RefundRecordEntity::getRefundNo, previous.getBusinessNo()));
            if (refund != null) {
                return new RefundResult(refund.getRefundNo(), refund.getStatus(), refund.getAmountCent());
            }
        }

        SalesOrderEntity order = salesOrderMapper.selectForUpdate(tenantId, storeId, orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        if (!OrderStatus.PENDING_VERIFY.matches(order.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "只有待核销订单可以整单退款");
        }
        String refundNo = "RF" + tenantId + randomPart(14);
        int changed = salesOrderMapper.updateStatus(tenantId, storeId, orderNo,
                OrderStatus.PENDING_VERIFY.code(), OrderStatus.REFUNDING.code());
        if (changed != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "订单状态已变化，请勿重复退款");
        }

        var external = wechatPayClient.refund(orderNo, refundNo, order.getPayAmountCent(), reason);
        RefundStatus refundStatus = external.success() ? RefundStatus.SUCCESS : RefundStatus.FAILED;
        insertRefundRecord(order, refundNo, refundStatus, reason);
        if (external.success()) {
            for (OrderLine line : orderLines(order)) {
                inventoryApplicationService.restoreAfterRefund(tenantId, storeId,
                        line.skuId(), line.quantity(), refundNo);
            }
            salesOrderMapper.updateStatus(tenantId, storeId, orderNo,
                    OrderStatus.REFUNDING.code(), OrderStatus.REFUNDED.code());
            verificationPersistenceService.cancelOrderCredential(tenantId, storeId, orderNo);
        }
        insertIdempotency(tenantId, idempotencyKey, BusinessType.ORDER_REFUND,
                refundNo, refundStatus.code());
        return new RefundResult(refundNo, refundStatus.code(), order.getPayAmountCent());
    }

    private OrderLine lockAndPrice(MerchantSceneScope scope, PersistentOrderLineCommand line, String orderNo) {
        InventoryApplicationService.PricedSku sku = inventoryApplicationService.lockAndPrice(
                scope.tenantId(), scope.storeId(), line.skuId(), line.quantity(), orderNo);
        return new OrderLine(sku.skuId(), sku.productName(), sku.specificationText(),
                sku.unitPriceCent(), sku.quantity(), sku.subtotalCent());
    }

    private void insertOrder(MerchantSceneScope scope, String orderNo, int amountCent,
                             String mobile, String pickupTime, String prepayId) {
        SalesOrderEntity order = new SalesOrderEntity();
        order.setTenantId(scope.tenantId());
        order.setStoreId(scope.storeId());
        order.setOrderNo(orderNo);
        order.setStatus(OrderStatus.PENDING_PAYMENT.code());
        order.setPayAmountCent(amountCent);
        order.setContactMobile(mobile);
        order.setPickupTimeText(pickupTime);
        order.setPrepayId(prepayId);
        salesOrderMapper.insert(order);
    }

    private void insertOrderItem(MerchantSceneScope scope, String orderNo, OrderLine line) {
        SalesOrderItemEntity item = new SalesOrderItemEntity();
        item.setTenantId(scope.tenantId());
        item.setStoreId(scope.storeId());
        item.setOrderNo(orderNo);
        item.setSkuId(line.skuId());
        item.setProductName(line.productName());
        item.setSkuText(line.specificationText());
        item.setAddonSnapshot(EMPTY_ADDON_SNAPSHOT);
        item.setUnitPriceCent(line.unitPriceCent());
        item.setAddonAmountCent(ZERO_ADDON_AMOUNT_CENT);
        item.setQuantity(line.quantity());
        item.setSubtotalCent(line.subtotalCent());
        salesOrderItemMapper.insert(item);
    }

    private void insertPaymentRecord(SalesOrderEntity order, String transactionId, int amountCent) {
        PaymentRecordEntity payment = new PaymentRecordEntity();
        payment.setTenantId(order.getTenantId());
        payment.setStoreId(order.getStoreId());
        payment.setOrderNo(order.getOrderNo());
        payment.setTransactionId(transactionId);
        payment.setAmountCent(amountCent);
        payment.setStatus(PaymentStatus.SUCCESS.code());
        payment.setPaidAt(LocalDateTime.now(BUSINESS_ZONE_ID));
        paymentRecordMapper.insert(payment);
    }

    private void insertRefundRecord(SalesOrderEntity order, String refundNo,
                                    RefundStatus status, String reason) {
        RefundRecordEntity refund = new RefundRecordEntity();
        refund.setTenantId(order.getTenantId());
        refund.setStoreId(order.getStoreId());
        refund.setOrderNo(order.getOrderNo());
        refund.setRefundNo(refundNo);
        refund.setAmountCent(order.getPayAmountCent());
        refund.setStatus(status.code());
        refund.setReason(reason);
        refundRecordMapper.insert(refund);
    }

    private BusinessIdempotencyRecordEntity findIdempotency(Long tenantId, BusinessType type, String key) {
        return idempotencyRecordMapper.selectOne(Wrappers.<BusinessIdempotencyRecordEntity>lambdaQuery()
                .eq(BusinessIdempotencyRecordEntity::getTenantId, tenantId)
                .eq(BusinessIdempotencyRecordEntity::getBusinessType, type.code())
                .eq(BusinessIdempotencyRecordEntity::getIdempotencyKey, key));
    }

    private void insertIdempotency(Long tenantId, String key, BusinessType type,
                                   String businessNo, String resultStatus) {
        BusinessIdempotencyRecordEntity record = new BusinessIdempotencyRecordEntity();
        record.setTenantId(tenantId);
        record.setIdempotencyKey(key);
        record.setBusinessType(type.code());
        record.setBusinessNo(businessNo);
        record.setResultStatus(resultStatus);
        idempotencyRecordMapper.insert(record);
    }

    private PersistentOrderResult findCreatedOrder(Long tenantId, String orderNo) {
        SalesOrderEntity order = salesOrderMapper.selectOne(Wrappers.<SalesOrderEntity>lambdaQuery()
                .eq(SalesOrderEntity::getTenantId, tenantId)
                .eq(SalesOrderEntity::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        return toResult(order);
    }

    private SalesOrderEntity findOrderForCallback(String orderNo) {
        SalesOrderEntity order = salesOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private PersistentOrderResult toResult(SalesOrderEntity order) {
        return new PersistentOrderResult(order.getOrderNo(), order.getPayAmountCent(), order.getStatus(),
                order.getPrepayId(), "", null, null);
    }

    private List<OrderLine> orderLines(SalesOrderEntity order) {
        return salesOrderItemMapper.selectList(Wrappers.<SalesOrderItemEntity>lambdaQuery()
                        .eq(SalesOrderItemEntity::getTenantId, order.getTenantId())
                        .eq(SalesOrderItemEntity::getStoreId, order.getStoreId())
                        .eq(SalesOrderItemEntity::getOrderNo, order.getOrderNo()))
                .stream()
                .map(item -> new OrderLine(item.getSkuId(), item.getProductName(), item.getSkuText(),
                        item.getUnitPriceCent(), item.getQuantity(), item.getSubtotalCent()))
                .toList();
    }

    private void validate(PersistentCreateOrderCommand command) {
        if (command == null || command.idempotencyKey() == null || command.idempotencyKey().isBlank()
                || command.contactMobile() == null || !command.contactMobile().matches("1\\d{10}")
                || command.lines() == null || command.lines().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "下单参数不合法");
        }
        if (command.lines().stream().anyMatch(line -> line.skuId() == null || line.quantity() <= 0)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商品数量不合法");
        }
    }

    private String randomPart(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }

    /** 创建订单命令。 */
    public record PersistentCreateOrderCommand(String idempotencyKey, String contactMobile,
                                               String pickupTimeText, List<PersistentOrderLineCommand> lines) {
    }

    /** 创建订单的单个商品命令。 */
    public record PersistentOrderLineCommand(Long skuId, int quantity) {
    }

    /** 商品订单处理结果。 */
    public record PersistentOrderResult(String orderNo, int payAmountCent, String status,
                                        String prepayId, String paymentParameters,
                                        String pickupNo, String verificationToken) {
    }

    /** 整单退款处理结果。 */
    public record RefundResult(String refundNo, String status, int amountCent) {
    }

    /** 订单商品成交快照。 */
    private record OrderLine(Long skuId, String productName, String specificationText,
                             int unitPriceCent, int quantity, int subtotalCent) {
    }

    /** 订单业务状态。 */
    private enum OrderStatus {
        PENDING_PAYMENT,
        PENDING_VERIFY,
        REFUNDING,
        REFUNDED;

        String code() {
            return name();
        }

        boolean matches(String value) {
            return code().equals(value);
        }
    }

    /** 幂等业务类型。 */
    private enum BusinessType {
        ORDER_CREATE,
        ORDER_REFUND;

        String code() {
            return name();
        }
    }

    /** 持久化操作结果。 */
    private enum PersistenceResult {
        SUCCESS;

        String code() {
            return name();
        }
    }

    /** 支付记录状态。 */
    private enum PaymentStatus {
        SUCCESS;

        String code() {
            return name();
        }
    }

    /** 退款记录状态。 */
    private enum RefundStatus {
        SUCCESS,
        FAILED;

        String code() {
            return name();
        }
    }
}
