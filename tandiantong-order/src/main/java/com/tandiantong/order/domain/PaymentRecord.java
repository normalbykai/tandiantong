package com.tandiantong.order.domain;

import java.time.Instant;

/** 订单支付记录。 */
public record PaymentRecord(
        Long paymentId,
        String orderNo,
        String transactionId,
        int amountCent,
        Instant paidAt
) {
}
