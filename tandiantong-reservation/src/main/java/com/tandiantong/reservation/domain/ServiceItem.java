package com.tandiantong.reservation.domain;

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
