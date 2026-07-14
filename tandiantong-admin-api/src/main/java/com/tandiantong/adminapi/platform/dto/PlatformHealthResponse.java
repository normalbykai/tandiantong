package com.tandiantong.adminapi.platform.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** 平台服务健康状态响应。 */
@Schema(description = "平台服务健康状态响应")
public record PlatformHealthResponse(
        @Schema(description = "平台服务状态描述", example = "平台服务正常")
        String status,

        @Schema(description = "检查时间，ISO-8601 格式", example = "2026-07-14T10:00:00Z")
        String checkedAt
) {
}
