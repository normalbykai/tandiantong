package com.tandiantong.reservation.domain;

import java.time.Instant;

public record ReservationRecord(
        Long reservationId,
        Long tenantId,
        Long storeId,
        Long serviceId,
        Long slotId,
        String reservationNo,
        ReservationStatus status,
        String contactMobile,
        String voucherCode,
        String transactionId,
        Instant createdAt
) {

    public ReservationRecord withStatus(ReservationStatus newStatus) {
        return new ReservationRecord(reservationId, tenantId, storeId, serviceId, slotId, reservationNo,
                newStatus, contactMobile, voucherCode, transactionId, createdAt);
    }

    public ReservationRecord confirmed(String newVoucherCode, String newTransactionId) {
        return new ReservationRecord(reservationId, tenantId, storeId, serviceId, slotId, reservationNo,
                ReservationStatus.CONFIRMED, contactMobile, newVoucherCode, newTransactionId, createdAt);
    }
}
