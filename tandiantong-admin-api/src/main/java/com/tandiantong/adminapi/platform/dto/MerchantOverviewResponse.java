package com.tandiantong.adminapi.platform.dto;

import com.tandiantong.security.tenant.MerchantProvisioningService;

import io.swagger.v3.oas.annotations.media.Schema;

/** 平台商户列表响应。 */
@Schema(description = "平台商户列表响应")
public class MerchantOverviewResponse {
    @Schema(description = "租户 ID", example = "1001")
    private final Long tenantId;

    @Schema(description = "商户名称", example = "春风小铺")
    private final String merchantName;

    @Schema(description = "商户管理员姓名", example = "张晓春")
    private final String adminName;

    @Schema(description = "脱敏后的管理员手机号", example = "138****8000")
    private final String adminMobileMasked;

    @Schema(description = "租户状态", example = "ENABLED")
    private final String status;

    @Schema(description = "支付配置状态", example = "NOT_CONFIGURED")
    private final String paymentConfigStatus;

    @Schema(description = "管理员账号状态", example = "ACTIVATED")
    private final String adminStatus;

    @Schema(description = "小程序商户入口码", example = "scene_xinghe_001")
    private final String sceneKey;

    private MerchantOverviewResponse(MerchantProvisioningService.MerchantOverview merchant) {
        this.tenantId = merchant.tenantId();
        this.merchantName = merchant.merchantName();
        this.adminName = merchant.adminName();
        this.adminMobileMasked = merchant.adminMobileMasked();
        this.status = merchant.status();
        this.paymentConfigStatus = merchant.paymentConfigStatus();
        this.adminStatus = merchant.adminStatus();
        this.sceneKey = merchant.sceneKey();
    }

    public static MerchantOverviewResponse from(
            MerchantProvisioningService.MerchantOverview merchant) {
        return new MerchantOverviewResponse(merchant);
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public String getAdminName() {
        return adminName;
    }

    public String getAdminMobileMasked() {
        return adminMobileMasked;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentConfigStatus() {
        return paymentConfigStatus;
    }

    public String getAdminStatus() {
        return adminStatus;
    }

    public String getSceneKey() {
        return sceneKey;
    }
}
