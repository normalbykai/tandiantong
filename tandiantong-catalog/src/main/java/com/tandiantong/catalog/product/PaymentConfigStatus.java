package com.tandiantong.catalog.product;

/** 商品业务使用的支付配置状态。 */
public enum PaymentConfigStatus {
    /** 租户尚未配置支付参数。 */
    NOT_CONFIGURED,
    /** 支付参数已提交，等待验证。 */
    PENDING_VERIFY,
    /** 支付参数已验证通过。 */
    VERIFIED
}
