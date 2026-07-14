package com.tandiantong.adminapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 后台登录响应。 */
@Schema(description = "后台登录响应")
public record LoginResponse(
        @Schema(description = "Sa-Token 访问令牌，调用受保护接口时放入 Authorization 请求头", example = "9f3f1c2a-1111-2222-3333-abcdef123456")
        String accessToken,

        @Schema(description = "登录身份权限域，平台为 PLATFORM，商户为 TENANT", example = "TENANT")
        String domain,

        @Schema(description = "当前登录用户展示名称", example = "张店长")
        String displayName
) {
}
