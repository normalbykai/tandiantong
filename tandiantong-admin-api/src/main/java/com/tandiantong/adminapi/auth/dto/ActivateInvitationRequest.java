package com.tandiantong.adminapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 商户管理员邀请激活请求。 */
@Schema(description = "商户管理员邀请激活请求")
public record ActivateInvitationRequest(
        @Schema(description = "商户开通后生成的邀请码", example = "INV-20260714-0001")
        @NotBlank(message = "邀请码不能为空")
        String invitationCode,

        @Schema(description = "商户管理员首次设置的登录密码", example = "Merchant@123456")
        @NotBlank(message = "密码不能为空")
        String password
) {
}
