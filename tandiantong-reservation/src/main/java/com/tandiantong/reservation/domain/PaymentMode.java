package com.tandiantong.reservation.domain;

/** 预约服务支付模式。 */
public enum PaymentMode {
    /** 免费预约，确认后直接生成凭证。 */
    FREE,
    /** 微信支付预约，支付成功后生成凭证。 */
    WECHAT_PAY
}
