package com.tandiantong.security.audit;

/** 结构化审计事件，避免业务服务拼接展示文案。 */
public class AuditEvent {
    private final AuditAction action;
    private final AuditTarget target;
    private Integer itemCount;
    private String policy;

    private AuditEvent(AuditAction action, AuditTarget target) {
        this.action = action;
        this.target = target;
    }

    public static AuditEvent of(AuditAction action, AuditTarget target) {
        return new AuditEvent(action, target);
    }

    public AuditEvent withItemCount(int itemCount) {
        this.itemCount = itemCount;
        return this;
    }

    public AuditEvent withPolicy(String policy) {
        this.policy = policy;
        return this;
    }

    public AuditAction getAction() {
        return action;
    }

    public AuditTarget getTarget() {
        return target;
    }

    public String renderDetail() {
        String name = target.displayName();
        return switch (action) {
            case PLATFORM_ACCOUNT_CREATED -> "已新增平台账号：" + name;
            case PLATFORM_ACCOUNT_UPDATED -> "已修改平台账号：" + name + "的资料或角色";
            case PLATFORM_ACCOUNT_ENABLED -> "已启用平台账号：" + name;
            case PLATFORM_ACCOUNT_DISABLED -> "已停用平台账号：" + name;
            case PLATFORM_ACCOUNT_UNLOCKED -> "已解除平台账号：" + name + "的临时登录锁定，并清零登录失败次数";
            case PLATFORM_ACCOUNT_PASSWORD_RESET -> "已按系统安全策略重置平台账号：" + name + "的密码，策略：" + policy;
            case PLATFORM_ROLE_CREATED -> "已新增平台角色：" + name;
            case PLATFORM_ROLE_UPDATED -> "已修改平台角色：" + name + "的名称或说明";
            case PLATFORM_ROLE_ENABLED -> "已启用平台角色：" + name;
            case PLATFORM_ROLE_DISABLED -> "已停用平台角色：" + name;
            case PLATFORM_ROLE_PERMISSIONS_UPDATED -> "已更新平台角色：" + name + "的权限，共 " + itemCount + " 项";
            case PLATFORM_DICTIONARY_ITEM_CREATED -> "已新增平台字典项：" + name;
            case PLATFORM_DICTIONARY_ITEM_UPDATED -> "已修改平台字典项：" + name + "的名称或排序";
            case PLATFORM_DICTIONARY_ITEM_ENABLED -> "已启用平台字典项：" + name;
            case PLATFORM_DICTIONARY_ITEM_DISABLED -> "已停用平台字典项：" + name;
            case PLATFORM_SYSTEM_CONFIG_UPDATED -> "已更新平台系统配置";
            case MERCHANT_PROVISIONED -> "已开通商户：" + name + "；初始状态为待启用";
            case MERCHANT_ENABLED -> "已启用商户：" + name + "；商户后台可登录，并可发起新的业务操作";
            case MERCHANT_DISABLED -> "已停用商户：" + name + "；商户后台将无法登录，且不能发起新的业务操作";
            case MERCHANT_INVITATION_REISSUED -> "已为商户：" + name + "重新生成管理员邀请码；旧邀请码已失效";
            case MERCHANT_STAFF_CREATED -> "已新增商户员工：" + name;
            case MERCHANT_STAFF_UPDATED -> "已修改商户员工：" + name + "的资料或角色";
            case MERCHANT_STAFF_ENABLED -> "已启用商户员工：" + name;
            case MERCHANT_STAFF_DISABLED -> "已停用商户员工：" + name;
            case MERCHANT_ROLE_CREATED -> "已新增商户角色：" + name;
            case MERCHANT_ROLE_UPDATED -> "已修改商户角色：" + name + "的名称或说明";
            case MERCHANT_ROLE_ENABLED -> "已启用商户角色：" + name;
            case MERCHANT_ROLE_DISABLED -> "已停用商户角色：" + name;
            case MERCHANT_ROLE_PERMISSIONS_UPDATED -> "已更新商户角色：" + name + "的权限，共 " + itemCount + " 项";
            case MERCHANT_SYSTEM_CONFIG_UPDATED -> "已更新商户展示设置";
            case ORDER_REFUND_REQUESTED -> "已提交商品订单：" + name + "的核销前整单退款申请";
            case BUSINESS_CREDENTIAL_VERIFIED -> "已完成业务凭证：" + name + "的核销";
        };
    }
}
