package com.tandiantong.order.app;

import com.tandiantong.integration.wechatpay.WechatPrepayResult;
import com.tandiantong.order.domain.OrderItemSnapshot;
import com.tandiantong.order.domain.SalesOrder;

import java.util.List;

/** 商品订单创建结果。 */
public record OrderCreationResult(
        SalesOrder order,
        List<OrderItemSnapshot> items,
        WechatPrepayResult prepay
) {
}
