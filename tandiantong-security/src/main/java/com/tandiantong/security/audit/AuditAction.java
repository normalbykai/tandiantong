package com.tandiantong.security.audit;

/** 可审计的后台业务动作，展示文案由审计事件统一生成。 */
public enum AuditAction {
    PLATFORM_ACCOUNT_CREATED("新增平台账号"),
    PLATFORM_ACCOUNT_UPDATED("编辑平台账号"),
    PLATFORM_ACCOUNT_ENABLED("启用平台账号"),
    PLATFORM_ACCOUNT_DISABLED("停用平台账号"),
    PLATFORM_ACCOUNT_UNLOCKED("解除平台账号锁定"),
    PLATFORM_ACCOUNT_PASSWORD_RESET("重置平台账号密码"),
    PLATFORM_ROLE_CREATED("新增平台角色"),
    PLATFORM_ROLE_UPDATED("编辑平台角色"),
    PLATFORM_ROLE_ENABLED("启用平台角色"),
    PLATFORM_ROLE_DISABLED("停用平台角色"),
    PLATFORM_ROLE_PERMISSIONS_UPDATED("配置平台角色权限"),
    PLATFORM_DICTIONARY_ITEM_CREATED("新增平台字典项"),
    PLATFORM_DICTIONARY_ITEM_UPDATED("编辑平台字典项"),
    PLATFORM_DICTIONARY_ITEM_ENABLED("启用平台字典项"),
    PLATFORM_DICTIONARY_ITEM_DISABLED("停用平台字典项"),
    PLATFORM_SYSTEM_CONFIG_UPDATED("更新平台系统配置"),
    MERCHANT_PROVISIONED("开通商户"),
    MERCHANT_ENABLED("启用商户"),
    MERCHANT_DISABLED("停用商户"),
    MERCHANT_INVITATION_REISSUED("重新生成商户邀请码"),
    MERCHANT_STAFF_CREATED("新增商户员工"),
    MERCHANT_STAFF_UPDATED("编辑商户员工"),
    MERCHANT_STAFF_ENABLED("启用商户员工"),
    MERCHANT_STAFF_DISABLED("停用商户员工"),
    MERCHANT_ROLE_CREATED("新增商户角色"),
    MERCHANT_ROLE_UPDATED("编辑商户角色"),
    MERCHANT_ROLE_ENABLED("启用商户角色"),
    MERCHANT_ROLE_DISABLED("停用商户角色"),
    MERCHANT_ROLE_PERMISSIONS_UPDATED("配置商户角色权限"),
    MERCHANT_SYSTEM_CONFIG_UPDATED("更新商户展示设置"),
    ORDER_REFUND_REQUESTED("订单退款"),
    BUSINESS_CREDENTIAL_VERIFIED("业务核销");

    private final String label;

    AuditAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
