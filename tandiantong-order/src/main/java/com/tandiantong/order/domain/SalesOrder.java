package com.tandiantong.order.domain;

import java.time.Instant;

public record SalesOrder(
        Long orderId,
        Long tenantId,
        Long storeId,
        Long customerId,
        String orderNo,
        OrderStatus status,
        int payAmountCent,
        String contactMobile,
        String pickupTimeText,
        String prepayId,
        Instant createdAt,
        Instant paidAt
) {

    public SalesOrder withStatus(OrderStatus newStatus) {
        return new SalesOrder(orderId, tenantId, storeId, customerId, orderNo, newStatus, payAmountCent,
                contactMobile, pickupTimeText, prepayId, createdAt, paidAt);
    }

    public SalesOrder paid(String transactionId) {
        return new SalesOrder(orderId, tenantId, storeId, customerId, orderNo, OrderStatus.PENDING_VERIFY,
                payAmountCent, contactMobile, pickupTimeText, prepayId, createdAt, Instant.now());
    }
}
