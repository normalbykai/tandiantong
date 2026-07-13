package com.tandiantong.order.domain;

/** 商品订单业务状态。 */
public enum OrderStatus {
    PENDING_PAYMENT,
    PENDING_VERIFY,
    COMPLETED,
    CANCELED,
    REFUNDING,
    REFUNDED
}
