package com.tandiantong.order.app;

import java.util.List;

public record CreateOrderCommand(
        String idempotencyKey,
        String contactMobile,
        String pickupTimeText,
        List<OrderSkuSelection> selections
) {
}
