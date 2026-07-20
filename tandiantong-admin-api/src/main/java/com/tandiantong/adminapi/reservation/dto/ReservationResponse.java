package com.tandiantong.adminapi.reservation.dto;

import com.tandiantong.reservation.app.ReservationPersistenceService;

import io.swagger.v3.oas.annotations.media.Schema;

/** 商户预约处理响应。 */
@Schema(description = "商户预约处理响应")
public class ReservationResponse {
    @Schema(description = "平台预约单号", example = "RS10001ABCDEF123456")
    private final String reservationNo;

    @Schema(description = "预约状态", example = "CANCELED")
    private final String status;

    @Schema(description = "预约取号，取消后可能为空", example = "A001")
    private final String pickupNo;

    private ReservationResponse(ReservationPersistenceService.ReservationResult result) {
        this.reservationNo = result.reservationNo();
        this.status = result.status();
        this.pickupNo = result.pickupNo();
    }

    public static ReservationResponse from(ReservationPersistenceService.ReservationResult result) {
        return new ReservationResponse(result);
    }

    public String getReservationNo() {
        return reservationNo;
    }

    public String getStatus() {
        return status;
    }

    public String getPickupNo() {
        return pickupNo;
    }
}
