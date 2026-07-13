package com.tandiantong.analytics.domain;

import java.math.BigDecimal;

/** 预约经营汇总指标。 */
public record ReservationSummary(
        int reservationCount,
        int canceledCount,
        int fulfilledCount,
        BigDecimal averageSlotUsageRate
) {
}
