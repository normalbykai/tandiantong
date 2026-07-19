package com.tandiantong.adminapi.reservation;

import com.tandiantong.adminapi.reservation.dto.CreateServiceRequest;
import com.tandiantong.adminapi.reservation.dto.CreateSlotRequest;
import com.tandiantong.adminapi.reservation.dto.ReservationResponse;
import com.tandiantong.adminapi.reservation.dto.ServiceResponse;
import com.tandiantong.adminapi.reservation.dto.SlotResponse;
import com.tandiantong.reservation.app.ReservationPersistenceService;
import com.tandiantong.reservation.tenant.TenantStoreScope;
import com.tandiantong.security.context.SecurityContextHolder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 商户后台预约服务、时段和取消接口。 */
@RestController
@RequestMapping("/api/admin/v1/reservations")
@Tag(name = "商户预约", description = "商户后台管理预约服务、预约时段和预约状态")
public class AdminReservationController {
    private final ReservationPersistenceService service;

    public AdminReservationController(ReservationPersistenceService service) {
        this.service = service;
    }

    @Operation(summary = "创建预约服务", description = "创建免费或付费预约服务项目")
    @PostMapping("/services")
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceResponse createService(@Valid @RequestBody CreateServiceRequest request) {
        return ServiceResponse.from(
                service.createService(
                        scope(),
                        new ReservationPersistenceService.CreateServiceCommand(
                                request.name(),
                                request.paymentMode(),
                                request.priceCent(),
                                request.durationMinutes())));
    }

    @Operation(summary = "创建预约时段", description = "为指定服务项目创建带容量限制的可预约时段")
    @PostMapping("/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public SlotResponse createSlot(@Valid @RequestBody CreateSlotRequest request) {
        return SlotResponse.from(
                service.createSlot(
                        scope(),
                        new ReservationPersistenceService.CreateSlotCommand(
                                request.serviceId(),
                                request.serviceDate(),
                                request.startTime(),
                                request.endTime(),
                                request.capacity())));
    }

    @Operation(summary = "取消预约", description = "取消当前租户和门店下尚未履约的预约")
    @PostMapping("/{reservationNo}/cancel")
    public ReservationResponse cancel(
            @Parameter(description = "平台预约单号", example = "RS10001ABCDEF123456", required = true)
                    @PathVariable("reservationNo")
                    String reservationNo) {
        return ReservationResponse.from(service.cancel(scope(), reservationNo));
    }

    private TenantStoreScope scope() {
        var user = SecurityContextHolder.currentUser();
        return new TenantStoreScope(user.tenantId(), user.storeId(), user.userId());
    }
}
