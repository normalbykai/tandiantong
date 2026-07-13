package com.tandiantong.miniapi.order;

import com.tandiantong.order.app.PersistentOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "小程序订单")
public class MiniOrderController {

    private final PersistentOrderService persistentOrderService;

    public MiniOrderController(PersistentOrderService persistentOrderService) {
        this.persistentOrderService = persistentOrderService;
    }

    @Operation(summary = "创建商品订单")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersistentOrderService.PersistentOrderResult create(@Valid @RequestBody CreateOrderRequest request) {
        return persistentOrderService.createOrder(request.sceneKey(), new PersistentOrderService.PersistentCreateOrderCommand(
                request.idempotencyKey(), request.contactMobile(), request.pickupTimeText(), request.lines().stream()
                        .map(line -> new PersistentOrderService.PersistentOrderLineCommand(line.skuId(), line.quantity())).toList()));
    }

    /** 创建商品订单请求。 */
    public record CreateOrderRequest(
            @NotBlank(message = "商户入口码不能为空") String sceneKey,
            @NotBlank(message = "幂等键不能为空") String idempotencyKey,
            @NotBlank(message = "联系电话不能为空") String contactMobile,
            @NotBlank(message = "取餐时间不能为空") String pickupTimeText,
            @NotEmpty(message = "订单至少需要一个商品") List<CreateOrderLineRequest> lines
    ) {}

    /** 创建订单商品明细请求。 */
    public record CreateOrderLineRequest(@NotNull(message = "SKU不能为空") Long skuId,
                                         @Positive(message = "商品数量必须大于零") int quantity) {}
}
