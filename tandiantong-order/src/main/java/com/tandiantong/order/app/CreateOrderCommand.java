package com.tandiantong.order.app;

import java.util.List;

/** 创建商品订单命令。 */
public record CreateOrderCommand(
        String idempotencyKey,
        String contactMobile,
        String pickupTimeText,
        List<OrderSkuSelection> selections
) {
}
