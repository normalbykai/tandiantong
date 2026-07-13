package com.tandiantong.analytics.domain;

/** 交易经营汇总指标。 */
public record TransactionSummary(
        int orderCount,
        int paidOrderCount,
        int grossAmountCent,
        int refundAmountCent,
        int netAmountCent,
        int pendingVerificationCount
) {
}
