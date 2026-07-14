package com.tandiantong.order.domain;

/** 订单退款状态。 */
public enum RefundStatus {
    /** 退款处理中。 */
    PROCESSING,
    /** 退款已成功。 */
    SUCCESS,
    /** 退款处理失败。 */
    FAILED
}
