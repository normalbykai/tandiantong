package com.tandiantong.verification.domain;

/** 核销凭证状态。 */
public enum VerificationStatus {
    /** 凭证已签发，等待核销。 */
    PENDING,
    /** 凭证已完成核销。 */
    VERIFIED,
    /** 凭证已取消，不允许核销。 */
    CANCELED
}
