package com.tandiantong.order.domain;

import java.time.Instant;

public record PaymentRecord(
        Long paymentId,
        String orderNo,
        String transactionId,
        int amountCent,
        Instant paidAt
) {
}
