package com.tandiantong.adminapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 平台管理员登录请求。 */
@Schema(description = "平台管理员登录请求")
public record PlatformLoginRequest(
        @Schema(description = "平台管理员账号或手机号", example = "13800000000")
        @NotBlank(message = "账号不能为空")
        String mobile,

        @Schema(description = "平台管理员登录密码", example = "Admin@123456")
        @NotBlank(message = "密码不能为空")
        String password
) {
}
