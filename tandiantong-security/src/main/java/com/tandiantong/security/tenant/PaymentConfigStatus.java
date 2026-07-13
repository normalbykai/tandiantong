package com.tandiantong.security.tenant;

/** 租户支付配置状态。 */
public enum PaymentConfigStatus {
    NOT_CONFIGURED,
    PENDING_VERIFY,
    VERIFIED,
    VERIFY_FAILED
}
