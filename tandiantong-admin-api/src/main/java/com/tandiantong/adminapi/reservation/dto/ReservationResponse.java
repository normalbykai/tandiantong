package com.tandiantong.adminapi.reservation.dto;

import com.tandiantong.reservation.app.ReservationPersistenceService;
import io.swagger.v3.oas.annotations.media.Schema;

/** 商户预约处理响应。 */
@Schema(description = "商户预约处理响应")
public class ReservationResponse {
    @Schema(description = "平台预约单号", example = "RS10001ABCDEF123456") private final String reservationNo;
    @Schema(description = "预约状态", example = "CANCELED") private final String status;
    @Schema(description = "预约核销凭证，取消后可能为空", example = "vk-abcdef1234567890abcdef1234567890") private final String voucherCode;

    private ReservationResponse(ReservationPersistenceService.ReservationResult result) {
        this.reservationNo = result.reservationNo(); this.status = result.status(); this.voucherCode = result.voucherCode();
    }
    public static ReservationResponse from(ReservationPersistenceService.ReservationResult result) { return new ReservationResponse(result); }
    public String getReservationNo() { return reservationNo; }
    public String getStatus() { return status; }
    public String getVoucherCode() { return voucherCode; }
}
