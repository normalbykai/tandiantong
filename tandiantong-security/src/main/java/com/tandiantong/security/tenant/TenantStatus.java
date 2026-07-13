package com.tandiantong.security.tenant;

/** 商户租户启停状态。 */
public enum TenantStatus {
    DRAFT,
    PENDING_ENABLE,
    NEEDS_REVISION,
    PENDING_REVIEW,
    ENABLED,
    DISABLED
}
