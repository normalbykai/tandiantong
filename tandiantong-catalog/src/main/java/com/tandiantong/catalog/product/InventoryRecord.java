package com.tandiantong.catalog.product;

import java.time.Instant;

public record InventoryRecord(
        Long recordId,
        Long tenantId,
        Long storeId,
        Long skuId,
        InventoryChangeType changeType,
        int quantity,
        int availableAfter,
        int lockedAfter,
        String businessNo,
        String reason,
        Long operatorUserId,
        Instant createdAt
) {
}
