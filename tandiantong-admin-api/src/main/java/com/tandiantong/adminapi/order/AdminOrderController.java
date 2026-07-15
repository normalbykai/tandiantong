package com.tandiantong.adminapi.order;

import com.tandiantong.adminapi.order.dto.RefundRequest;
import com.tandiantong.adminapi.order.dto.RefundResponse;
import com.tandiantong.order.app.PersistentOrderService;
import com.tandiantong.security.audit.OperationAuditService;
import com.tandiantong.security.context.SecurityContextHolder;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "商户订单", description = "商户后台处理商品订单退款")
public class AdminOrderController {

    private final PersistentOrderService service;
    private final OperationAuditService operationAuditService;

    public AdminOrderController(PersistentOrderService service, OperationAuditService operationAuditService) {
        this.service = service;
        this.operationAuditService = operationAuditService;
    }

    @Operation(summary = "申请订单整单退款", description = "仅支持核销前整单退款，同一退款动作应复用幂等键")
    @PostMapping("/{orderNo}/refund")
    @SaCheckPermission("order:refund:create")
    public RefundResponse refund(
            @Parameter(description = "平台商品订单号", example = "SO10001ABCDEF123456", required = true)
            @PathVariable("orderNo") String orderNo, @Valid @RequestBody RefundRequest request) {
        var user = SecurityContextHolder.currentUser();
        var result = service.refund(user.tenantId(), user.storeId(), orderNo, request.idempotencyKey(), request.reason());
        operationAuditService.record(user, "订单退款", "商品订单", orderNo, "已提交核销前整单退款申请");
        return RefundResponse.from(result);
    }
}
