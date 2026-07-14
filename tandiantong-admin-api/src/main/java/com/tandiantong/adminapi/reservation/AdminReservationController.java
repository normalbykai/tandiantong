package com.tandiantong.adminapi.reservation;

import com.tandiantong.adminapi.reservation.dto.CreateServiceRequest;
import com.tandiantong.adminapi.reservation.dto.CreateSlotRequest;
import com.tandiantong.reservation.app.ReservationPersistenceService;
import com.tandiantong.reservation.tenant.TenantStoreScope;
import com.tandiantong.security.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "商户预约")
public class AdminReservationController {
    private final ReservationPersistenceService service;
    public AdminReservationController(ReservationPersistenceService service){this.service=service;}
    @Operation(summary = "创建预约服务")
    @PostMapping("/services") @ResponseStatus(HttpStatus.CREATED)
    public ReservationPersistenceService.ServiceResult createService(@Valid @RequestBody CreateServiceRequest request){return service.createService(scope(),new ReservationPersistenceService.CreateServiceCommand(request.name(),request.paymentMode(),request.priceCent(),request.durationMinutes()));}
    @Operation(summary = "创建预约时段")
    @PostMapping("/slots") @ResponseStatus(HttpStatus.CREATED)
    public ReservationPersistenceService.SlotResult createSlot(@Valid @RequestBody CreateSlotRequest request){return service.createSlot(scope(),new ReservationPersistenceService.CreateSlotCommand(request.serviceId(),request.serviceDate(),request.startTime(),request.endTime(),request.capacity()));}
    @Operation(summary = "取消预约")
    @PostMapping("/{reservationNo}/cancel")
    public ReservationPersistenceService.ReservationResult cancel(@PathVariable("reservationNo") String reservationNo){return service.cancel(scope(),reservationNo);}
    private TenantStoreScope scope(){var user=SecurityContextHolder.currentUser();return new TenantStoreScope(user.tenantId(),user.storeId(),user.userId());}
}
