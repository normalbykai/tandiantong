package com.tandiantong.adminapi.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

/** 创建预约时段请求。 */
@Schema(description = "创建预约时段请求")
public record CreateSlotRequest(
        @Schema(description = "服务项目 ID", example = "1")
        @NotNull(message = "服务项目不能为空")
        Long serviceId,

        @Schema(description = "服务日期", example = "2026-07-15")
        @NotNull(message = "服务日期不能为空")
        LocalDate serviceDate,

        @Schema(description = "时段开始时间，使用 HH:mm 格式", example = "09:00")
        @NotBlank(message = "开始时间不能为空")
        String startTime,

        @Schema(description = "时段结束时间，使用 HH:mm 格式", example = "09:30")
        @NotBlank(message = "结束时间不能为空")
        String endTime,

        @Schema(description = "时段可预约容量", example = "5")
        @Positive(message = "容量必须大于零")
        int capacity
) {
}
