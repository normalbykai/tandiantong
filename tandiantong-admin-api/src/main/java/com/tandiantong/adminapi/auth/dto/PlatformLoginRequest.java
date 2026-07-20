package com.tandiantong.adminapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/** 平台管理员登录请求。 */
@Getter
@Setter
@Schema(description = "平台管理员登录请求")
public class PlatformLoginRequest {
    @Schema(description = "平台管理员账号或手机号", example = "13900000000")
    @NotBlank(message = "账号不能为空")
    private String mobile;

    @Schema(description = "平台管理员登录密码", example = "Platform@123")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "是否七天内自动登录", example = "true")
    private boolean rememberMe;
}
