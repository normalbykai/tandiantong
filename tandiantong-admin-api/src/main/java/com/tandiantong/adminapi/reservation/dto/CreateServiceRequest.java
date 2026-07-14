package com.tandiantong.adminapi.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/** 创建预约服务请求。 */
@Schema(description = "创建预约服务请求")
public record CreateServiceRequest(
        @Schema(description = "服务项目名称", example = "肩颈放松")
        @NotBlank(message = "服务名称不能为空")
        String name,

        @Schema(description = "支付模式，FREE 表示免费预约，PAID 表示付费预约", example = "FREE")
        @NotBlank(message = "支付模式不能为空")
        String paymentMode,

        @Schema(description = "服务价格，单位为分；免费预约填 0", example = "0")
        @PositiveOrZero(message = "服务价格不能小于零")
        int priceCent,

        @Schema(description = "服务时长，单位为分钟", example = "30")
        @Positive(message = "服务时长必须大于零")
        int durationMinutes
) {
}
