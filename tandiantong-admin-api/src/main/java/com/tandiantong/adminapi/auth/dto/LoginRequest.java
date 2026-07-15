package com.tandiantong.adminapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** 商户后台登录请求。 */
@Schema(description = "商户后台登录请求")
public record LoginRequest(
        @Schema(description = "商户后台用户手机号", example = "admin")
        @Pattern(regexp = "1\\d{10}", message = "手机号格式不正确")
        String mobile,

        @Schema(description = "商户后台用户登录密码", example = "admin")
        @NotBlank(message = "密码不能为空")
        String password
) {
}
