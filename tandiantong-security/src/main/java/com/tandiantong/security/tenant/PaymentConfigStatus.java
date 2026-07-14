package com.tandiantong.security.tenant;

/** 租户支付配置状态。 */
public enum PaymentConfigStatus {
    /** 尚未配置支付参数。 */
    NOT_CONFIGURED,
    /** 支付参数已提交，等待验证。 */
    PENDING_VERIFY,
    /** 支付参数已验证通过。 */
    VERIFIED,
    /** 支付参数验证失败，需要重新配置。 */
    VERIFY_FAILED
}
