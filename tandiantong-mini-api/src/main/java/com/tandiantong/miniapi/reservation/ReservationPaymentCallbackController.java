package com.tandiantong.miniapi.reservation;

import com.tandiantong.miniapi.reservation.dto.ReservationPaymentCallbackRequest;
import com.tandiantong.miniapi.reservation.dto.ReservationResponse;
import com.tandiantong.reservation.app.ReservationPersistenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 预约微信支付回调接口，接收并验证服务预约支付结果。 */
@RestController
@RequestMapping("/api/callback/wechat-pay/reservations")
@Tag(name = "预约支付回调", description = "接收并处理微信支付平台的预约异步支付结果")
public class ReservationPaymentCallbackController {

    private final ReservationPersistenceService reservationPersistenceService;

    public ReservationPaymentCallbackController(ReservationPersistenceService reservationPersistenceService) {
        this.reservationPersistenceService = reservationPersistenceService;
    }

    @Operation(summary = "处理预约支付回调", description = "校验回调签名和支付金额，并按合法状态机确认付费预约")
    @PostMapping
    public ReservationResponse callback(@Valid @RequestBody ReservationPaymentCallbackRequest request) {
        return ReservationResponse.from(reservationPersistenceService.confirmPayment(
                request.getReservationNo(), request.getTransactionId(), request.getAmountCent(),
                request.getSignature()));
    }
}
