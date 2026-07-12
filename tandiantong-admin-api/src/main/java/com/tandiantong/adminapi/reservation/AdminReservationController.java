package com.tandiantong.adminapi.reservation;

import com.tandiantong.reservation.app.ReservationPersistenceService;
import com.tandiantong.reservation.tenant.TenantStoreScope;
import com.tandiantong.security.context.SecurityContextHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/reservations")
public class AdminReservationController {
    private final ReservationPersistenceService service;
    public AdminReservationController(ReservationPersistenceService service){this.service=service;}
    @PostMapping("/services") @ResponseStatus(HttpStatus.CREATED)
    public ReservationPersistenceService.ServiceResult createService(@Valid @RequestBody CreateServiceRequest request){return service.createService(scope(),new ReservationPersistenceService.CreateServiceCommand(request.name(),request.paymentMode(),request.priceCent(),request.durationMinutes()));}
    @PostMapping("/slots") @ResponseStatus(HttpStatus.CREATED)
    public ReservationPersistenceService.SlotResult createSlot(@Valid @RequestBody CreateSlotRequest request){return service.createSlot(scope(),new ReservationPersistenceService.CreateSlotCommand(request.serviceId(),request.serviceDate(),request.startTime(),request.endTime(),request.capacity()));}
    @PostMapping("/{reservationNo}/cancel")
    public ReservationPersistenceService.ReservationResult cancel(@PathVariable("reservationNo") String reservationNo){return service.cancel(scope(),reservationNo);}
    private TenantStoreScope scope(){var user=SecurityContextHolder.currentUser();return new TenantStoreScope(user.tenantId(),user.storeId(),user.userId());}
    public record CreateServiceRequest(@NotBlank String name,@NotBlank String paymentMode,@PositiveOrZero int priceCent,@Positive int durationMinutes){}
    public record CreateSlotRequest(@NotNull Long serviceId,@NotNull LocalDate serviceDate,@NotBlank String startTime,@NotBlank String endTime,@Positive int capacity){}
}
