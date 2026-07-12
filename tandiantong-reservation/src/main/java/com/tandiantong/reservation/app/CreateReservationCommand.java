package com.tandiantong.reservation.app;

public record CreateReservationCommand(
        String idempotencyKey,
        Long serviceId,
        Long slotId,
        String contactMobile
) {
}
