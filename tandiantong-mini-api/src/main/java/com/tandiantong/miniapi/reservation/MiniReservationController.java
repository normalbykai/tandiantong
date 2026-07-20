package com.tandiantong.miniapi.reservation;

import com.tandiantong.miniapi.reservation.dto.MiniServiceResponse;
import com.tandiantong.miniapi.reservation.dto.ReservationResponse;
import com.tandiantong.miniapi.reservation.dto.ReserveRequest;
import com.tandiantong.reservation.app.ReservationPersistenceService;
import com.tandiantong.security.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 小程序预约服务查询与提交接口。 */
@RestController
@RequestMapping("/api/mini/v1/reservations")
@Tag(name = "小程序预约", description = "顾客查询可预约服务并提交预约")
public class MiniReservationController {
    private final ReservationPersistenceService service;

    public MiniReservationController(ReservationPersistenceService service) {
        this.service = service;
    }

    @Operation(summary = "查询可预约服务", description = "查询指定商户入口下可预约的服务项目、时段和剩余容量")
    @GetMapping("/services")
    public List<MiniServiceResponse> services(
            @Parameter(description = "商户小程序入口码", example = "scene_xinghe_001", required = true)
                    @RequestParam("scene")
                    String scene) {
        return service.listByScene(scene).stream().map(MiniServiceResponse::from).toList();
    }

    @Operation(summary = "提交预约", description = "使用幂等键占用预约时段容量并创建顾客预约")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse reserve(@Valid @RequestBody ReserveRequest request) {
        return ReservationResponse.from(
                service.reserve(
                        request.sceneKey(),
                        new ReservationPersistenceService.ReserveCommand(
                                request.idempotencyKey(),
                                request.serviceId(),
                                request.slotId(),
                                request.contactMobile())));
    }
}
