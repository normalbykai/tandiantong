package com.tandiantong.analytics.domain;

import java.time.LocalDate;

/** 预约经营统计事实。 */
public record AnalyticsReservationFact(
        Long tenantId,
        Long storeId,
        String reservationNo,
        LocalDate businessDate,
        ReservationFactStatus status,
        int slotCapacity,
        int slotUsedCapacity
) {
}
