package com.tandiantong.miniapi.order.dto;

import com.tandiantong.order.app.PersistentOrderService;
import io.swagger.v3.oas.annotations.media.Schema;

/** 小程序商品订单处理结果。 */
@Schema(description = "小程序商品订单处理结果")
public class OrderResponse {

    @Schema(description = "平台商品订单号", example = "SO10001ABCDEF123456")
    private final String orderNo;

    @Schema(description = "订单应付金额，单位为分", example = "3600")
    private final int payAmountCent;

    @Schema(description = "订单状态", example = "PENDING_PAYMENT")
    private final String status;

    @Schema(description = "微信预支付标识，支付成功回调时可能为空", example = "wx-prepay-001")
    private final String prepayId;

    @Schema(description = "小程序调起微信支付所需参数，支付成功回调时可能为空")
    private final String paymentParameters;

    @Schema(description = "取餐号，支付成功前可能为空", example = "A001")
    private final String pickupNo;

    @Schema(description = "安全核销令牌，支付成功前可能为空", example = "vk-abcdef1234567890abcdef1234567890")
    private final String verificationToken;

    private OrderResponse(PersistentOrderService.PersistentOrderResult result) {
        this.orderNo = result.orderNo();
        this.payAmountCent = result.payAmountCent();
        this.status = result.status();
        this.prepayId = result.prepayId();
        this.paymentParameters = result.paymentParameters();
        this.pickupNo = result.pickupNo();
        this.verificationToken = result.verificationToken();
    }

    public static OrderResponse from(PersistentOrderService.PersistentOrderResult result) {
        return new OrderResponse(result);
    }

    public String getOrderNo() { return orderNo; }
    public int getPayAmountCent() { return payAmountCent; }
    public String getStatus() { return status; }
    public String getPrepayId() { return prepayId; }
    public String getPaymentParameters() { return paymentParameters; }
    public String getPickupNo() { return pickupNo; }
    public String getVerificationToken() { return verificationToken; }
}
