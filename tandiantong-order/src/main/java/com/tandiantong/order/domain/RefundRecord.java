package com.tandiantong.order.domain;

import java.time.Instant;

/** 订单退款记录。 */
public record RefundRecord(
        Long refundId,
        String orderNo,
        String refundNo,
        int amountCent,
        RefundStatus status,
        String reason,
        Instant createdAt
) {
}
