package com.tandiantong.adminapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/** 商户后台登录请求。 */
@Getter
@Setter
@Schema(description = "商户后台登录请求")
public class LoginRequest {
    @Schema(description = "商户后台用户手机号", example = "13900000000")
    @Pattern(regexp = "1\\d{10}", message = "手机号格式不正确")
    private String mobile;

    @Schema(description = "商户后台用户登录密码", example = "Merchant@123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "是否七天内自动登录", example = "true")
    private boolean rememberMe;
}
