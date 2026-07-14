package com.tandiantong.miniapi.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** 顾客预约请求。 */
@Schema(description = "小程序顾客预约请求")
public record ReserveRequest(
        @Schema(description = "商户小程序入口码", example = "scene_xinghe_001")
        @NotBlank(message = "商户入口码不能为空")
        String sceneKey,

        @Schema(description = "预约幂等键，同一次提交重试时保持不变", example = "reserve-20260714-0001")
        @NotBlank(message = "幂等键不能为空")
        String idempotencyKey,

        @Schema(description = "服务项目 ID", example = "1")
        @NotNull(message = "服务项目不能为空")
        Long serviceId,

        @Schema(description = "预约时段 ID", example = "10")
        @NotNull(message = "预约时段不能为空")
        Long slotId,

        @Schema(description = "顾客联系电话", example = "13800000000")
        @NotBlank(message = "联系电话不能为空")
        String contactMobile
) {
}
