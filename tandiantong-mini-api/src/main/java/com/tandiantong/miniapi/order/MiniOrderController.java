package com.tandiantong.miniapi.order;

import com.tandiantong.miniapi.order.dto.CreateOrderRequest;
import com.tandiantong.miniapi.order.dto.OrderResponse;
import com.tandiantong.order.app.PersistentOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
                        .map(line -> new PersistentOrderService.PersistentOrderLineCommand(line.skuId(), line.quantity())).toList())));
    }
}
