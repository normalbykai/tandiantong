package com.tandiantong.order.app;

import com.tandiantong.integration.wechatpay.WechatPrepayResult;
import com.tandiantong.order.domain.OrderItemSnapshot;
import com.tandiantong.order.domain.SalesOrder;

import java.util.List;

public record OrderCreationResult(
        SalesOrder order,
        List<OrderItemSnapshot> items,
        WechatPrepayResult prepay
) {
}
