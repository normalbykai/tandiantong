package com.tandiantong.adminapi.verification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** 核销凭证请求。 */
@Schema(description = "核销凭证请求")
public record VerifyRequest(
        @Schema(description = "不可猜测的核销令牌", example = "vk-abcdef1234567890abcdef1234567890")
        @NotBlank(message = "核销凭证不能为空")
        String token,

        @Schema(description = "核销原因，留空时按正常核销处理", example = "顾客到店核销")
        String reason
) {
}
