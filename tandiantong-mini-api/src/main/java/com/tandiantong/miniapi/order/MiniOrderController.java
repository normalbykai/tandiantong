package com.tandiantong.miniapi.order;

import com.tandiantong.miniapi.order.dto.CreateOrderRequest;
import com.tandiantong.miniapi.order.dto.OrderResponse;
import com.tandiantong.order.app.PersistentOrderService;
import com.tandiantong.security.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * 小程序商品订单接口，负责接收顾客下单请求。
 */
@RestController
@RequestMapping("/api/mini/v1/orders")
@Tag(name = "小程序订单", description = "顾客在微信小程序中创建商品订单")
public class MiniOrderController {

    private final PersistentOrderService persistentOrderService;

    public MiniOrderController(PersistentOrderService persistentOrderService) {
        this.persistentOrderService = persistentOrderService;
    }

    @Operation(summary = "创建商品订单", description = "根据商户入口码、商品明细和幂等键创建待支付商品订单")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return OrderResponse.from(persistentOrderService.createOrder(request.sceneKey(), new PersistentOrderService.PersistentCreateOrderCommand(
                request.idempotencyKey(), request.contactMobile(), request.pickupTimeText(), request.lines().stream()
                        .map(line -> new PersistentOrderService.PersistentOrderLineCommand(line.skuId(), line.quantity(), line.addonNames())).toList())));
    }

    @Operation(summary = "查询订单列表", description = "按当前租户门店和联系电话查询顾客订单列表")
    @GetMapping
    public List<PersistentOrderService.OrderSummaryView> list(@RequestParam("contactMobile") String contactMobile,
                                                             @RequestParam(value = "status", required = false) String status) {
        var user = SecurityContextHolder.currentUser();
        return persistentOrderService.listCustomerOrders(user.tenantId(), user.storeId(), contactMobile, status);
    }

    @Operation(summary = "查询订单详情", description = "按订单号查询当前顾客订单详情")
    @GetMapping("/{orderNo}")
    public PersistentOrderService.OrderDetailView detail(@PathVariable("orderNo") String orderNo,
                                                         @RequestParam("contactMobile") String contactMobile) {
        var user = SecurityContextHolder.currentUser();
        return persistentOrderService.getCustomerOrderDetail(user.tenantId(), user.storeId(), orderNo, contactMobile);
    }

    @Operation(summary = "取消待支付订单", description = "顾客取消未支付订单并释放库存")
    @DeleteMapping("/{orderNo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable("orderNo") String orderNo, @RequestParam("reason") String reason) {
        var user = SecurityContextHolder.currentUser();
        persistentOrderService.cancelPendingOrder(user.tenantId(), user.storeId(), orderNo, reason);
    }
}
