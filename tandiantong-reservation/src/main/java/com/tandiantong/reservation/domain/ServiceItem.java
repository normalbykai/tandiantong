package com.tandiantong.reservation.domain;

/** 可预约服务项目。 */
public record ServiceItem(
        Long serviceId,
        Long tenantId,
        Long storeId,
        String serviceName,
        PaymentMode paymentMode,
        int priceCent,
        int durationMinutes
) {
}
