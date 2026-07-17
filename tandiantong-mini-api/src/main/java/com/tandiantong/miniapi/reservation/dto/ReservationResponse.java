package com.tandiantong.miniapi.reservation.dto;

import com.tandiantong.reservation.app.ReservationPersistenceService;
import io.swagger.v3.oas.annotations.media.Schema;

/** 小程序预约处理结果。 */
@Schema(description = "小程序预约处理结果")
public class ReservationResponse {
    @Schema(description = "平台预约单号", example = "RS10001ABCDEF123456") private final String reservationNo;
    @Schema(description = "预约状态", example = "CONFIRMED") private final String status;
    @Schema(description = "预约支付金额，单位为分", example = "9900") private final int payAmountCent;
    @Schema(description = "微信预支付标识，免费预约为空", example = "LOCAL-PREPAY-YY10001ABCDEF123456") private final String prepayId;
    @Schema(description = "微信支付参数，本地开发环境为模拟 nonce", example = "mock-pay-params") private final String paymentParameters;
    @Schema(description = "预约取号，待支付时为空", example = "A001") private final String pickupNo;
    @Schema(description = "核销令牌明文，只在首次签发时返回", example = "vk-abcdef1234567890abcdef1234567890") private final String verificationToken;

    private ReservationResponse(ReservationPersistenceService.ReservationResult result) {
        this.reservationNo = result.reservationNo();
        this.status = result.status();
        this.payAmountCent = result.payAmountCent();
        this.prepayId = result.prepayId();
        this.paymentParameters = result.paymentParameters();
        this.pickupNo = result.pickupNo();
        this.verificationToken = result.verificationToken();
    }
    public static ReservationResponse from(ReservationPersistenceService.ReservationResult result) { return new ReservationResponse(result); }
    public String getReservationNo() { return reservationNo; }
    public String getStatus() { return status; }
    public int getPayAmountCent() { return payAmountCent; }
    public String getPrepayId() { return prepayId; }
    public String getPaymentParameters() { return paymentParameters; }
    public String getPickupNo() { return pickupNo; }
    public String getVerificationToken() { return verificationToken; }
}
