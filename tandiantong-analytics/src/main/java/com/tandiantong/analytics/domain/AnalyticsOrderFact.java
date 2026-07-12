package com.tandiantong.analytics.domain;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsOrderFact(
        Long tenantId,
        Long storeId,
        String orderNo,
        LocalDate businessDate,
        OrderFactStatus status,
        int grossAmountCent,
        int refundAmountCent,
        int quantity,
        String productName,
        String skuName,
        List<String> addonNames
) {
}
