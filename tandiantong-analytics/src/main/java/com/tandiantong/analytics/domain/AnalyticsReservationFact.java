package com.tandiantong.analytics.domain;

import java.time.LocalDate;

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
