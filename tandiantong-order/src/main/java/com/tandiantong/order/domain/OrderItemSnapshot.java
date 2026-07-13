package com.tandiantong.order.domain;

import java.util.List;

/** 订单商品成交快照。 */
public record OrderItemSnapshot(
        Long itemId,
        String orderNo,
        Long skuId,
        String productName,
        String skuText,
        List<String> addonSnapshot,
        int unitPriceCent,
        int addonAmountCent,
        int quantity,
        int subtotalCent
) {
}
