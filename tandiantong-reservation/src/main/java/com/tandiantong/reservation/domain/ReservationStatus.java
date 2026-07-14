package com.tandiantong.reservation.domain;

/** 服务预约业务状态。 */
public enum ReservationStatus {
    /** 付费预约已提交，等待支付。 */
    PENDING_PAYMENT,
    /** 预约已确认，等待到店履约。 */
    CONFIRMED,
    /** 预约已完成履约。 */
    FULFILLED,
    /** 预约已取消。 */
    CANCELED
}
