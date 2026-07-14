package com.tandiantong.security.tenant;

/** 商户租户启停状态。 */
public enum TenantStatus {
    /** 租户草稿，尚未提交启用流程。 */
    DRAFT,
    /** 租户等待平台启用。 */
    PENDING_ENABLE,
    /** 租户资料需要修改后重新提交。 */
    NEEDS_REVISION,
    /** 租户资料等待平台审核。 */
    PENDING_REVIEW,
    /** 租户已启用，可进行正常业务操作。 */
    ENABLED,
    /** 租户已停用，禁止新增交易和业务写操作。 */
    DISABLED
}
