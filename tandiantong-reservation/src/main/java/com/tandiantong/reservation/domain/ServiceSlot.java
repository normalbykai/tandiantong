package com.tandiantong.reservation.domain;

import java.time.LocalDate;

public record ServiceSlot(
        Long slotId,
        Long tenantId,
        Long storeId,
        Long serviceId,
        LocalDate serviceDate,
        String startTime,
        String endTime,
        int capacity,
        int usedCapacity,
        boolean paused,
        long version
) {

    public ServiceSlot occupy() {
        return new ServiceSlot(slotId, tenantId, storeId, serviceId, serviceDate, startTime, endTime,
                capacity, usedCapacity + 1, paused, version + 1);
    }

    public ServiceSlot release() {
        return new ServiceSlot(slotId, tenantId, storeId, serviceId, serviceDate, startTime, endTime,
                capacity, Math.max(0, usedCapacity - 1), paused, version + 1);
    }
}
