package com.tandiantong.miniapi.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/** 微信支付回调请求。 */
@Schema(description = "微信支付回调请求")
public record PaymentCallbackRequest(
        @Schema(description = "平台商品订单号", example = "SO10001ABCDEF123456")
        @NotBlank(message = "订单号不能为空")
        String orderNo,

        @Schema(description = "微信支付交易流水号", example = "4200000000202607140000000001")
        @NotBlank(message = "支付流水号不能为空")
        String transactionId,

        @Schema(description = "支付金额，单位为分", example = "3600")
        @PositiveOrZero(message = "支付金额不能小于零")
        int amountCent,

        @Schema(description = "微信支付回调签名", example = "mock-signature")
        @NotBlank(message = "签名不能为空")
        String signature
) {
}
