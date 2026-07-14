package com.tandiantong.adminapi.order;

import com.tandiantong.adminapi.order.dto.RefundRequest;
import com.tandiantong.order.app.PersistentOrderService;
import com.tandiantong.security.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户后台订单接口，提供待核销订单整单退款能力。
 */
@RestController
@RequestMapping("/api/admin/v1/orders")
@Tag(name = "商户订单")
public class AdminOrderController {
    private final PersistentOrderService service;
    public AdminOrderController(PersistentOrderService service){this.service=service;}
    @Operation(summary = "申请订单整单退款")
    @PostMapping("/{orderNo}/refund")
    public PersistentOrderService.RefundResult refund(@PathVariable("orderNo") String orderNo,@Valid @RequestBody RefundRequest request){var user=SecurityContextHolder.currentUser();return service.refund(user.tenantId(),user.storeId(),orderNo,request.idempotencyKey(),request.reason());}
}
