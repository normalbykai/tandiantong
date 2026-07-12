package com.tandiantong.miniapi.reservation;

import com.tandiantong.reservation.app.ReservationPersistenceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mini/v1/reservations")
public class MiniReservationController {
    private final ReservationPersistenceService service;
    public MiniReservationController(ReservationPersistenceService service){this.service=service;}
    @GetMapping("/services") public List<ReservationPersistenceService.MiniService> services(@RequestParam("scene") String scene){return service.listByScene(scene);}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public ReservationPersistenceService.ReservationResult reserve(@Valid @RequestBody ReserveRequest request){return service.reserve(request.sceneKey(),new ReservationPersistenceService.ReserveCommand(request.idempotencyKey(),request.serviceId(),request.slotId(),request.contactMobile()));}
    public record ReserveRequest(@NotBlank String sceneKey,@NotBlank String idempotencyKey,@NotNull Long serviceId,@NotNull Long slotId,@NotBlank String contactMobile){}
}
