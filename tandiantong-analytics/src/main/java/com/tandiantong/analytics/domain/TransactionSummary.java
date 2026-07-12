package com.tandiantong.analytics.domain;

public record TransactionSummary(
        int orderCount,
        int paidOrderCount,
        int grossAmountCent,
        int refundAmountCent,
        int netAmountCent,
        int pendingVerificationCount
) {
}
