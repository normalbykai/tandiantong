package com.tandiantong.reservation.app;

/** 创建服务预约命令。 */
public record CreateReservationCommand(
        String idempotencyKey,
        Long serviceId,
        Long slotId,
        String contactMobile
) {
}
