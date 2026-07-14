package com.tandiantong.order.domain;

/** 商品订单业务状态。 */
public enum OrderStatus {
    /** 订单已创建，等待顾客完成支付。 */
    PENDING_PAYMENT,
    /** 订单已支付，等待商户核销履约。 */
    PENDING_VERIFY,
    /** 订单已核销并完成履约。 */
    COMPLETED,
    /** 订单已取消。 */
    CANCELED,
    /** 订单正在发起或处理退款。 */
    REFUNDING,
    /** 订单已完成退款。 */
    REFUNDED
}
