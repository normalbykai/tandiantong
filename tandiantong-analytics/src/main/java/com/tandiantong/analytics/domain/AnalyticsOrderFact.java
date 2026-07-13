package com.tandiantong.analytics.domain;

import java.time.LocalDate;
import java.util.List;

/** 订单经营统计事实。 */
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
