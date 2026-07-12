package com.tandiantong.analytics.domain;

import java.math.BigDecimal;

public record ReservationSummary(
        int reservationCount,
        int canceledCount,
        int fulfilledCount,
        BigDecimal averageSlotUsageRate
) {
}
