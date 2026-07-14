package com.tandiantong.adminapi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 商户开通响应。 */
@Schema(description = "商户开通响应")
public record MerchantProvisioningResponse(
        @Schema(description = "租户 ID", example = "1")
        Long tenantId,

        @Schema(description = "默认门店 ID", example = "1")
        Long storeId,

        @Schema(description = "商户名称", example = "星河便当")
        String merchantName,

        @Schema(description = "默认门店名称", example = "星河便当默认门店")
        String storeName,

        @Schema(description = "商户管理员激活邀请码", example = "INV-20260714-0001")
        String invitationCode,

        @Schema(description = "邀请码过期时间", example = "2026-07-21T10:00:00")
        String invitationExpiresAt,

        @Schema(description = "小程序商户入口码", example = "scene_xinghe_001")
        String sceneKey,

        @Schema(description = "租户支付配置状态", example = "NOT_CONFIGURED")
        String paymentConfigStatus
) {
}
