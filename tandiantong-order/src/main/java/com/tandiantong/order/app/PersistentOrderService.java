package com.tandiantong.order.app;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tandiantong.catalog.inventory.CatalogAddonPricingService;
import com.tandiantong.catalog.inventory.CatalogAddonPricingService.AddonQuote;
import com.tandiantong.catalog.inventory.InventoryApplicationService;
import com.tandiantong.framework.common.api.ErrorCode;
import com.tandiantong.framework.common.exception.BusinessException;
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

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** 商品订单应用服务，负责创建、查询、取消、支付确认、退款和重试。 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "tandiantong.security",
        name = "database-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PersistentOrderService {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final String ORDER_DESCRIPTION = "摊点通商品订单";

    private final SalesOrderMapper salesOrderMapper;
    private final SalesOrderItemMapper salesOrderItemMapper;
    private final BusinessIdempotencyRecordMapper idempotencyRecordMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final RefundRecordMapper refundRecordMapper;
    private final CatalogAddonPricingService catalogAddonPricingService;
    private final InventoryApplicationService inventoryApplicationService;
    private final MerchantSceneService merchantSceneService;
    private final WechatPayClient wechatPayClient;
    private final VerificationPersistenceService verificationPersistenceService;

    @Transactional
    public PersistentOrderResult createOrder(
            String sceneKey, PersistentCreateOrderCommand command) {
        MerchantSceneScope scope = merchantSceneService.resolveEnabledScene(sceneKey);
        validate(command);
        BusinessIdempotencyRecordEntity previous =
                findIdempotency(
                        scope.tenantId(), BusinessType.ORDER_CREATE, command.idempotencyKey());
        if (previous != null) {
            return findCreatedOrder(scope.tenantId(), previous.getBusinessNo());
        }

        String orderNo = "SO" + scope.tenantId() + randomPart(16);
        List<OrderLine> lines = new ArrayList<>();
        for (PersistentOrderLineCommand lineCommand : command.lines()) {
            lines.add(lockAndPrice(scope, lineCommand, orderNo));
        }
        int amountCent = lines.stream().mapToInt(OrderLine::subtotalCent).sum();
        WechatPrepayResult prepay =
                wechatPayClient.createPrepay(orderNo, amountCent, ORDER_DESCRIPTION);
        insertOrder(
                scope,
                orderNo,
                amountCent,
                command.contactMobile(),
                command.pickupTimeText(),
                prepay.prepayId());
        lines.forEach(line -> insertOrderItem(scope, orderNo, line));
        insertIdempotency(
                scope.tenantId(),
                command.idempotencyKey(),
                BusinessType.ORDER_CREATE,
                orderNo,
                PersistenceResult.SUCCESS.code());
        return new PersistentOrderResult(
                orderNo,
                amountCent,
                OrderStatus.PENDING_PAYMENT.code(),
                prepay.prepayId(),
                prepay.payNonce(),
                null,
                null);
    }

    @Transactional
    public PersistentOrderResult confirmPayment(
            String orderNo, String transactionId, int amountCent, String signature) {
        SalesOrderEntity order = findOrderForCallback(orderNo);
        if (!wechatPayClient.verifyCallback(orderNo, transactionId, amountCent, signature)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "微信支付回调验签失败");
        }
        if (OrderStatus.PENDING_VERIFY.matches(order.getStatus())) {
            return toResult(order);
        }
        if (!OrderStatus.PENDING_PAYMENT.matches(order.getStatus())
                || order.getPayAmountCent() != amountCent) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "订单支付状态或金额不正确");
        }
        int updated =
                salesOrderMapper.updatePaidStatus(
                        order.getTenantId(),
                        orderNo,
                        OrderStatus.PENDING_PAYMENT.code(),
                        OrderStatus.PENDING_VERIFY.code(),
                        LocalDateTime.now(BUSINESS_ZONE_ID));
        if (updated == 0) {
            return findCreatedOrder(order.getTenantId(), orderNo);
        }
        insertPaymentRecord(order, transactionId, amountCent);
        for (OrderLine line : orderLines(order)) {
            inventoryApplicationService.confirmPayment(
                    order.getTenantId(),
                    order.getStoreId(),
                    line.skuId(),
                    line.quantity(),
                    orderNo);
        }
        VerificationPersistenceService.Credential credential =
                verificationPersistenceService.issueOrderCredential(
                        order.getTenantId(), order.getStoreId(), orderNo, "商品订单 " + orderNo);
        return new PersistentOrderResult(
                orderNo,
                amountCent,
                OrderStatus.PENDING_VERIFY.code(),
                order.getPrepayId(),
                "",
                credential.pickupNo(),
                credential.token());
    }

    @Transactional
    public RefundResult refund(
            Long tenantId, Long storeId, String orderNo, String idempotencyKey, String reason) {
        BusinessIdempotencyRecordEntity previous =
                findIdempotency(tenantId, BusinessType.ORDER_REFUND, idempotencyKey);
        if (previous != null) {
            RefundRecordEntity refund =
                    refundRecordMapper.selectOne(
                            Wrappers.<RefundRecordEntity>lambdaQuery()
                                    .eq(RefundRecordEntity::getTenantId, tenantId)
                                    .eq(RefundRecordEntity::getRefundNo, previous.getBusinessNo()));
            if (refund != null) {
                return new RefundResult(
                        refund.getRefundNo(), refund.getStatus(), refund.getAmountCent());
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
        int changed =
                salesOrderMapper.updateStatus(
                        tenantId,
                        storeId,
                        orderNo,
                        OrderStatus.PENDING_VERIFY.code(),
                        OrderStatus.REFUNDING.code());
        if (changed != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "订单状态已变化，请勿重复退款");
        }
        var external = wechatPayClient.refund(orderNo, refundNo, order.getPayAmountCent(), reason);
        RefundStatus refundStatus = external.success() ? RefundStatus.SUCCESS : RefundStatus.FAILED;
        insertRefundRecord(
                order,
                refundNo,
                refundStatus,
                reason,
                0,
                external.success() ? null : external.message(),
                external.success() ? "SUCCESS" : "PENDING_RETRY");
        if (external.success()) {
            restoreOrderInventory(order, refundNo);
            salesOrderMapper.updateStatus(
                    tenantId,
                    storeId,
                    orderNo,
                    OrderStatus.REFUNDING.code(),
                    OrderStatus.REFUNDED.code());
            verificationPersistenceService.cancelOrderCredential(tenantId, storeId, orderNo);
        }
        insertIdempotency(
                tenantId, idempotencyKey, BusinessType.ORDER_REFUND, refundNo, refundStatus.code());
        return new RefundResult(refundNo, refundStatus.code(), order.getPayAmountCent());
    }

    public List<OrderSummaryView> listCustomerOrders(
            Long tenantId, Long storeId, String contactMobile, String status) {
        return salesOrderMapper
                .selectList(
                        Wrappers.<SalesOrderEntity>lambdaQuery()
                                .eq(SalesOrderEntity::getTenantId, tenantId)
                                .eq(SalesOrderEntity::getStoreId, storeId)
                                .eq(
                                        contactMobile != null && !contactMobile.isBlank(),
                                        SalesOrderEntity::getContactMobile,
                                        contactMobile)
                                .eq(
                                        status != null && !status.isBlank(),
                                        SalesOrderEntity::getStatus,
                                        status)
                                .orderByDesc(SalesOrderEntity::getCreatedAt))
                .stream()
                .map(this::toSummaryView)
                .toList();
    }

    public OrderDetailView getCustomerOrderDetail(
            Long tenantId, Long storeId, String orderNo, String contactMobile) {
        SalesOrderEntity order =
                salesOrderMapper.selectOne(
                        Wrappers.<SalesOrderEntity>lambdaQuery()
                                .eq(SalesOrderEntity::getTenantId, tenantId)
                                .eq(SalesOrderEntity::getStoreId, storeId)
                                .eq(SalesOrderEntity::getOrderNo, orderNo)
                                .eq(
                                        contactMobile != null && !contactMobile.isBlank(),
                                        SalesOrderEntity::getContactMobile,
                                        contactMobile));
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        List<OrderItemView> items = orderLines(order).stream().map(this::toItemView).toList();
        List<RefundView> refunds =
                refundRecordMapper
                        .selectList(
                                Wrappers.<RefundRecordEntity>lambdaQuery()
                                        .eq(RefundRecordEntity::getTenantId, tenantId)
                                        .eq(RefundRecordEntity::getStoreId, storeId)
                                        .eq(RefundRecordEntity::getOrderNo, orderNo)
                                        .orderByDesc(RefundRecordEntity::getCreatedAt))
                        .stream()
                        .map(this::toRefundView)
                        .toList();
        PaymentRecordEntity payment =
                paymentRecordMapper.selectOne(
                        Wrappers.<PaymentRecordEntity>lambdaQuery()
                                .eq(PaymentRecordEntity::getTenantId, tenantId)
                                .eq(PaymentRecordEntity::getStoreId, storeId)
                                .eq(PaymentRecordEntity::getOrderNo, orderNo));
        return new OrderDetailView(
                toSummaryView(order),
                items,
                refunds,
                payment == null ? null : payment.getTransactionId());
    }

    @Transactional
    public void cancelPendingOrder(Long tenantId, Long storeId, String orderNo, String reason) {
        SalesOrderEntity order = salesOrderMapper.selectForUpdate(tenantId, storeId, orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        if (!OrderStatus.PENDING_PAYMENT.matches(order.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "只有待支付订单可以取消");
        }
        restoreOrderInventory(order, orderNo);
        int changed =
                salesOrderMapper.updateCanceledStatus(
                        tenantId,
                        storeId,
                        orderNo,
                        OrderStatus.PENDING_PAYMENT.code(),
                        OrderStatus.CANCELED.code(),
                        LocalDateTime.now(BUSINESS_ZONE_ID),
                        reason);
        if (changed != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "订单状态已变化，请勿重复取消");
        }
    }

    @Transactional
    public int cancelExpiredPendingOrders(Long tenantId, Long storeId, LocalDateTime expireBefore) {
        List<SalesOrderEntity> candidates =
                salesOrderMapper.selectList(
                        Wrappers.<SalesOrderEntity>lambdaQuery()
                                .eq(SalesOrderEntity::getTenantId, tenantId)
                                .eq(SalesOrderEntity::getStoreId, storeId)
                                .eq(SalesOrderEntity::getStatus, OrderStatus.PENDING_PAYMENT.code())
                                .le(SalesOrderEntity::getExpireAt, expireBefore));
        int count = 0;
        for (SalesOrderEntity order : candidates) {
            cancelPendingOrder(tenantId, storeId, order.getOrderNo(), "订单超时自动取消");
            count++;
        }
        return count;
    }

    @Transactional
    public RefundResult retryFailedRefund(
            Long tenantId, Long storeId, String orderNo, String idempotencyKey, String reason) {
        RefundRecordEntity latestRefund = findLatestRefundRecord(tenantId, storeId, orderNo);
        if (latestRefund == null || !RefundStatus.FAILED.code().equals(latestRefund.getStatus())) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "当前订单没有可重试的退款失败记录");
        }
        Integer retryCount =
                latestRefund.getRetryCount() == null ? 0 : latestRefund.getRetryCount();
        if (retryCount >= 3) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "退款重试已达上限，请人工排查");
        }
        SalesOrderEntity order = salesOrderMapper.selectForUpdate(tenantId, storeId, orderNo);
        if (order == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        String refundNo = "RF" + tenantId + randomPart(14);
        var external = wechatPayClient.refund(orderNo, refundNo, order.getPayAmountCent(), reason);
        RefundStatus refundStatus = external.success() ? RefundStatus.SUCCESS : RefundStatus.FAILED;
        insertRefundRecord(
                order,
                refundNo,
                refundStatus,
                reason,
                retryCount + 1,
                external.success() ? null : external.message(),
                external.success()
                        ? "SUCCESS"
                        : (retryCount + 1 >= 3 ? "MANUAL_REVIEW" : "PENDING_RETRY"));
        if (external.success()) {
            restoreOrderInventory(order, refundNo);
            salesOrderMapper.updateStatus(
                    tenantId,
                    storeId,
                    orderNo,
                    OrderStatus.REFUNDING.code(),
                    OrderStatus.REFUNDED.code());
            verificationPersistenceService.cancelOrderCredential(tenantId, storeId, orderNo);
        }
        insertIdempotency(
                tenantId, idempotencyKey, BusinessType.ORDER_REFUND, refundNo, refundStatus.code());
        return new RefundResult(refundNo, refundStatus.code(), order.getPayAmountCent());
    }

    private OrderLine lockAndPrice(MerchantSceneScope scope, PersistentOrderLineCommand line, String orderNo) {
        InventoryApplicationService.PricedSku sku =
                inventoryApplicationService.lockAndPrice(
                        scope.tenantId(), scope.storeId(), line.skuId(), line.quantity(), orderNo);
        AddonQuote addonQuote =
                catalogAddonPricingService.quoteAddonSelection(
                        scope.tenantId(), scope.storeId(), sku.productId(), line.addonNames());
        return new OrderLine(
                sku.skuId(),
                sku.productName(),
                sku.specificationText(),
                sku.unitPriceCent(),
                addonQuote.addonAmountCent(),
                sku.quantity(),
                (sku.unitPriceCent() + addonQuote.addonAmountCent()) * sku.quantity(),
                addonQuote.addonNames());
    }

    private void insertOrder(
            MerchantSceneScope scope,
            String orderNo,
            int amountCent,
            String mobile,
            String pickupTime,
            String prepayId) {
        SalesOrderEntity order = new SalesOrderEntity();
        order.setTenantId(scope.tenantId());
        order.setStoreId(scope.storeId());
        order.setOrderNo(orderNo);
        order.setStatus(OrderStatus.PENDING_PAYMENT.code());
        order.setPayAmountCent(amountCent);
        order.setContactMobile(mobile);
        order.setPickupTimeText(pickupTime);
        order.setPrepayId(prepayId);
        order.setExpireAt(LocalDateTime.now(BUSINESS_ZONE_ID).plusMinutes(15));
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
        item.setAddonSnapshot(String.join(",", line.addonNames()));
        item.setUnitPriceCent(line.unitPriceCent());
        item.setAddonAmountCent(line.addonAmountCent());
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
        payment.setStatus("SUCCESS");
        payment.setPaidAt(LocalDateTime.now(BUSINESS_ZONE_ID));
        paymentRecordMapper.insert(payment);
    }

    private void insertRefundRecord(
            SalesOrderEntity order, String refundNo, RefundStatus status, String reason) {
        insertRefundRecord(
                order,
                refundNo,
                status,
                reason,
                0,
                null,
                status == RefundStatus.SUCCESS ? "SUCCESS" : "PENDING_RETRY");
    }

    private void insertRefundRecord(
            SalesOrderEntity order,
            String refundNo,
            RefundStatus status,
            String reason,
            int retryCount,
            String lastErrorMessage,
            String reviewStatus) {
        RefundRecordEntity refund = new RefundRecordEntity();
        refund.setTenantId(order.getTenantId());
        refund.setStoreId(order.getStoreId());
        refund.setOrderNo(order.getOrderNo());
        refund.setRefundNo(refundNo);
        refund.setAmountCent(order.getPayAmountCent());
        refund.setStatus(status.code());
        refund.setReason(reason);
        refund.setRetryCount(retryCount);
        refund.setNextRetryAt(
                status == RefundStatus.FAILED
                        ? LocalDateTime.now(BUSINESS_ZONE_ID).plusMinutes(15)
                        : null);
        refund.setLastErrorMessage(lastErrorMessage);
        refund.setReviewStatus(reviewStatus);
        refundRecordMapper.insert(refund);
    }

    private RefundRecordEntity findLatestRefundRecord(Long tenantId, Long storeId, String orderNo) {
        return refundRecordMapper.selectOne(
                Wrappers.<RefundRecordEntity>lambdaQuery()
                        .eq(RefundRecordEntity::getTenantId, tenantId)
                        .eq(RefundRecordEntity::getStoreId, storeId)
                        .eq(RefundRecordEntity::getOrderNo, orderNo)
                        .orderByDesc(RefundRecordEntity::getCreatedAt)
                        .last("limit 1"));
    }

    private BusinessIdempotencyRecordEntity findIdempotency(
            Long tenantId, BusinessType type, String key) {
        return idempotencyRecordMapper.selectOne(
                Wrappers.<BusinessIdempotencyRecordEntity>lambdaQuery()
                        .eq(BusinessIdempotencyRecordEntity::getTenantId, tenantId)
                        .eq(BusinessIdempotencyRecordEntity::getBusinessType, type.code())
                        .eq(BusinessIdempotencyRecordEntity::getIdempotencyKey, key));
    }

    private void insertIdempotency(
            Long tenantId, String key, BusinessType type, String businessNo, String resultStatus) {
        BusinessIdempotencyRecordEntity record = new BusinessIdempotencyRecordEntity();
        record.setTenantId(tenantId);
        record.setIdempotencyKey(key);
        record.setBusinessType(type.code());
        record.setBusinessNo(businessNo);
        record.setResultStatus(resultStatus);
        idempotencyRecordMapper.insert(record);
    }

    private PersistentOrderResult findCreatedOrder(Long tenantId, String orderNo) {
        SalesOrderEntity order =
                salesOrderMapper.selectOne(
                        Wrappers.<SalesOrderEntity>lambdaQuery()
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
        return new PersistentOrderResult(
                order.getOrderNo(),
                order.getPayAmountCent(),
                order.getStatus(),
                order.getPrepayId(),
                "",
                null,
                null);
    }

    private OrderSummaryView toSummaryView(SalesOrderEntity order) {
        return new OrderSummaryView(
                order.getOrderNo(),
                order.getStatus(),
                order.getPayAmountCent(),
                order.getContactMobile(),
                order.getPickupTimeText(),
                order.getCreatedAt(),
                order.getExpireAt());
    }

    private OrderItemView toItemView(OrderLine line) {
        return new OrderItemView(
                line.skuId(),
                line.productName(),
                line.specificationText(),
                line.unitPriceCent(),
                line.addonAmountCent(),
                line.quantity(),
                line.subtotalCent(),
                line.addonNames());
    }

    private RefundView toRefundView(RefundRecordEntity refund) {
        return new RefundView(
                refund.getRefundNo(),
                refund.getStatus(),
                refund.getAmountCent(),
                refund.getRetryCount(),
                refund.getReviewStatus(),
                refund.getLastErrorMessage(),
                refund.getNextRetryAt());
    }

    private List<OrderLine> orderLines(SalesOrderEntity order) {
        return salesOrderItemMapper
                .selectList(
                        Wrappers.<SalesOrderItemEntity>lambdaQuery()
                                .eq(SalesOrderItemEntity::getTenantId, order.getTenantId())
                                .eq(SalesOrderItemEntity::getStoreId, order.getStoreId())
                                .eq(SalesOrderItemEntity::getOrderNo, order.getOrderNo()))
                .stream()
                .map(
                        item ->
                                new OrderLine(
                                        item.getSkuId(),
                                        item.getProductName(),
                                        item.getSkuText(),
                                        item.getUnitPriceCent(),
                                        item.getAddonAmountCent(),
                                        item.getQuantity(),
                                        item.getSubtotalCent(),
                                        item.getAddonSnapshot() == null
                                                        || item.getAddonSnapshot().isBlank()
                                                ? List.of()
                                                : List.of(item.getAddonSnapshot().split(","))))
                .toList();
    }

    private void restoreOrderInventory(SalesOrderEntity order, String businessNo) {
        for (OrderLine line : orderLines(order)) {
            inventoryApplicationService.restoreAfterRefund(
                    order.getTenantId(),
                    order.getStoreId(),
                    line.skuId(),
                    line.quantity(),
                    businessNo);
        }
    }

    private void validate(PersistentCreateOrderCommand command) {
        if (command == null
                || command.idempotencyKey() == null
                || command.idempotencyKey().isBlank()
                || command.contactMobile() == null
                || !command.contactMobile().matches("1\\d{10}")
                || command.lines() == null
                || command.lines().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "下单参数不合法");
        }
        if (command.lines().stream()
                .anyMatch(line -> line.skuId() == null || line.quantity() <= 0)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商品数量不合法");
        }
    }

    private String randomPart(int length) {
        return UUID.randomUUID().toString().replace("-", "").substring(0, length);
    }

    /** 创建订单命令。 */
    public static class PersistentCreateOrderCommand {
        private final String idempotencyKey;
        private final String contactMobile;
        private final String pickupTimeText;
        private final List<PersistentOrderLineCommand> lines;

        public PersistentCreateOrderCommand(
                String idempotencyKey,
                String contactMobile,
                String pickupTimeText,
                List<PersistentOrderLineCommand> lines) {
            this.idempotencyKey = idempotencyKey;
            this.contactMobile = contactMobile;
            this.pickupTimeText = pickupTimeText;
            this.lines = lines;
        }

        public String idempotencyKey() {
            return idempotencyKey;
        }

        public String contactMobile() {
            return contactMobile;
        }

        public String pickupTimeText() {
            return pickupTimeText;
        }

        public List<PersistentOrderLineCommand> lines() {
            return lines;
        }
    }

    /** 创建订单的单个商品命令。 */
    public static class PersistentOrderLineCommand {
        private final Long skuId;
        private final int quantity;
        private final List<String> addonNames;

        public PersistentOrderLineCommand(Long skuId, int quantity) {
            this(skuId, quantity, List.of());
        }

        public PersistentOrderLineCommand(Long skuId, int quantity, List<String> addonNames) {
            this.skuId = skuId;
            this.quantity = quantity;
            this.addonNames = addonNames == null ? List.of() : List.copyOf(addonNames);
        }

        public Long skuId() {
            return skuId;
        }

        public int quantity() {
            return quantity;
        }

        public List<String> addonNames() {
            return addonNames;
        }
    }

    /** 商品订单处理结果。 */
    public record PersistentOrderResult(
            String orderNo,
            int payAmountCent,
            String status,
            String prepayId,
            String paymentParameters,
            String pickupNo,
            String verificationToken) {}

    /** 整单退款处理结果。 */
    public record RefundResult(String refundNo, String status, int amountCent) {}

    /** 顾客订单列表项。 */
    @Schema(description = "顾客订单列表项")
    public static class OrderSummaryView {
        @Schema(description = "平台商品订单号", example = "SO10001ABCDEF123456")
        private final String orderNo;
        @Schema(description = "订单状态", example = "PENDING_PAYMENT")
        private final String status;
        @Schema(description = "订单实付金额，单位为分", example = "3600")
        private final int payAmountCent;
        @Schema(description = "顾客联系电话", example = "13800000000")
        private final String contactMobile;
        @Schema(description = "顾客自填取餐时间", example = "2026-07-17 18:30")
        private final String pickupTimeText;
        @Schema(description = "订单创建时间")
        private final LocalDateTime createdAt;
        @Schema(description = "待支付订单过期时间")
        private final LocalDateTime expireAt;

        public OrderSummaryView(
                String orderNo,
                String status,
                int payAmountCent,
                String contactMobile,
                String pickupTimeText,
                LocalDateTime createdAt,
                LocalDateTime expireAt) {
            this.orderNo = orderNo;
            this.status = status;
            this.payAmountCent = payAmountCent;
            this.contactMobile = contactMobile;
            this.pickupTimeText = pickupTimeText;
            this.createdAt = createdAt;
            this.expireAt = expireAt;
        }

        public String getOrderNo() {
            return orderNo;
        }

        public String getStatus() {
            return status;
        }

        public int getPayAmountCent() {
            return payAmountCent;
        }

        public String getContactMobile() {
            return contactMobile;
        }

        public String getPickupTimeText() {
            return pickupTimeText;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public LocalDateTime getExpireAt() {
            return expireAt;
        }
    }

    /** 顾客订单详情。 */
    @Schema(description = "顾客订单详情")
    public static class OrderDetailView {
        @Schema(description = "订单概要")
        private final OrderSummaryView order;
        @Schema(description = "订单商品明细")
        private final List<OrderItemView> items;
        @Schema(description = "订单退款记录")
        private final List<RefundView> refunds;
        @Schema(description = "微信支付交易流水号，未支付时为空", example = "4200000000202607140000000001")
        private final String transactionId;

        public OrderDetailView(
                OrderSummaryView order,
                List<OrderItemView> items,
                List<RefundView> refunds,
                String transactionId) {
            this.order = order;
            this.items = List.copyOf(items);
            this.refunds = List.copyOf(refunds);
            this.transactionId = transactionId;
        }

        public OrderSummaryView getOrder() {
            return order;
        }

        public List<OrderItemView> getItems() {
            return items;
        }

        public List<RefundView> getRefunds() {
            return refunds;
        }

        public String getTransactionId() {
            return transactionId;
        }
    }

    /** 顾客订单明细。 */
    @Schema(description = "顾客订单商品明细")
    public static class OrderItemView {
        @Schema(description = "商品 SKU ID", example = "11")
        private final Long skuId;
        @Schema(description = "成交时商品名称", example = "桂花拿铁")
        private final String productName;
        @Schema(description = "成交时 SKU 规格", example = "杯型:中杯;温度:热")
        private final String skuText;
        @Schema(description = "SKU 单价，单位为分", example = "1800")
        private final int unitPriceCent;
        @Schema(description = "加料金额，单位为分", example = "300")
        private final int addonAmountCent;
        @Schema(description = "购买数量", example = "2")
        private final int quantity;
        @Schema(description = "小计金额，单位为分", example = "4200")
        private final int subtotalCent;
        @Schema(description = "加料名称快照")
        private final List<String> addonNames;

        public OrderItemView(
                Long skuId,
                String productName,
                String skuText,
                int unitPriceCent,
                int addonAmountCent,
                int quantity,
                int subtotalCent,
                List<String> addonNames) {
            this.skuId = skuId;
            this.productName = productName;
            this.skuText = skuText;
            this.unitPriceCent = unitPriceCent;
            this.addonAmountCent = addonAmountCent;
            this.quantity = quantity;
            this.subtotalCent = subtotalCent;
            this.addonNames = List.copyOf(addonNames);
        }

        public Long getSkuId() {
            return skuId;
        }

        public String getProductName() {
            return productName;
        }

        public String getSkuText() {
            return skuText;
        }

        public int getUnitPriceCent() {
            return unitPriceCent;
        }

        public int getAddonAmountCent() {
            return addonAmountCent;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getSubtotalCent() {
            return subtotalCent;
        }

        public List<String> getAddonNames() {
            return addonNames;
        }
    }

    /** 退款明细视图。 */
    @Schema(description = "订单退款明细")
    public static class RefundView {
        @Schema(description = "平台退款单号", example = "RF10001ABCDEF123456")
        private final String refundNo;
        @Schema(description = "退款状态", example = "SUCCESS")
        private final String status;
        @Schema(description = "退款金额，单位为分", example = "3600")
        private final int amountCent;
        @Schema(description = "已重试次数", example = "1")
        private final Integer retryCount;
        @Schema(description = "人工排查状态", example = "PENDING_RETRY")
        private final String reviewStatus;
        @Schema(description = "最后一次退款错误信息")
        private final String lastErrorMessage;
        @Schema(description = "下次重试时间")
        private final LocalDateTime nextRetryAt;

        public RefundView(
                String refundNo,
                String status,
                int amountCent,
                Integer retryCount,
                String reviewStatus,
                String lastErrorMessage,
                LocalDateTime nextRetryAt) {
            this.refundNo = refundNo;
            this.status = status;
            this.amountCent = amountCent;
            this.retryCount = retryCount;
            this.reviewStatus = reviewStatus;
            this.lastErrorMessage = lastErrorMessage;
            this.nextRetryAt = nextRetryAt;
        }

        public String getRefundNo() {
            return refundNo;
        }

        public String getStatus() {
            return status;
        }

        public int getAmountCent() {
            return amountCent;
        }

        public Integer getRetryCount() {
            return retryCount;
        }

        public String getReviewStatus() {
            return reviewStatus;
        }

        public String getLastErrorMessage() {
            return lastErrorMessage;
        }

        public LocalDateTime getNextRetryAt() {
            return nextRetryAt;
        }
    }

    private record OrderLine(
            Long skuId,
            String productName,
            String specificationText,
            int unitPriceCent,
            int addonAmountCent,
            int quantity,
            int subtotalCent,
            List<String> addonNames) {}

    private enum OrderStatus {
        PENDING_PAYMENT,
        PENDING_VERIFY,
        REFUNDING,
        REFUNDED,
        CANCELED,
        COMPLETED;

        String code() {
            return name();
        }

        boolean matches(String value) {
            return code().equals(value);
        }
    }

    private enum BusinessType {
        ORDER_CREATE,
        ORDER_REFUND;

        String code() {
            return name();
        }
    }

    private enum PersistenceResult {
        SUCCESS;

        String code() {
            return name();
        }
    }

    private enum RefundStatus {
        SUCCESS,
        FAILED;

        String code() {
            return name();
        }
    }
}
