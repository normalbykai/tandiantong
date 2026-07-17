package com.tandiantong.miniapi.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

/** 预约微信支付回调请求。 */
@Schema(description = "预约微信支付回调请求")
public class ReservationPaymentCallbackRequest {
    @Schema(description = "平台预约单号", example = "YY10001ABCDEF123456")
    @NotBlank(message = "预约单号不能为空")
    private String reservationNo;

    @Schema(description = "微信支付交易流水号", example = "4200000000202607170000000001")
    @NotBlank(message = "支付流水号不能为空")
    private String transactionId;

    @Schema(description = "支付金额，单位为分", example = "9900")
    @PositiveOrZero(message = "支付金额不能小于零")
    private int amountCent;

    @Schema(description = "微信支付回调签名", example = "mock-signature")
    @NotBlank(message = "签名不能为空")
    private String signature;

    public String getReservationNo() { return reservationNo; }
    public void setReservationNo(String reservationNo) { this.reservationNo = reservationNo; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public int getAmountCent() { return amountCent; }
    public void setAmountCent(int amountCent) { this.amountCent = amountCent; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}
