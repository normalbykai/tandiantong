package com.tandiantong.adminapi.order;

import com.tandiantong.order.app.PersistentOrderService;
import com.tandiantong.security.context.SecurityContextHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/v1/orders")
public class AdminOrderController {
    private final PersistentOrderService service;
    public AdminOrderController(PersistentOrderService service){this.service=service;}
    @PostMapping("/{orderNo}/refund")
    public PersistentOrderService.RefundResult refund(@PathVariable("orderNo") String orderNo,@Valid @RequestBody RefundRequest request){var user=SecurityContextHolder.currentUser();return service.refund(user.tenantId(),user.storeId(),orderNo,request.idempotencyKey(),request.reason());}
    public record RefundRequest(@NotBlank String idempotencyKey,@NotBlank String reason){}
}
