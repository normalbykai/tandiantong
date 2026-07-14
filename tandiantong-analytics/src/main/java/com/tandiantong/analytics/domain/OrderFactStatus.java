package com.tandiantong.analytics.domain;

/** 订单统计事实状态。 */
public enum OrderFactStatus {
    /** 订单已支付。 */
    PAID,
    /** 订单待核销。 */
    PENDING_VERIFY,
    /** 订单已退款。 */
    REFUNDED
}
