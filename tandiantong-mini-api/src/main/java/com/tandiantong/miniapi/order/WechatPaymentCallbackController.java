package com.tandiantong.miniapi.order;

import com.tandiantong.miniapi.order.dto.PaymentCallbackRequest;
import com.tandiantong.order.app.PersistentOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信支付回调接口，接收并验证商品订单支付结果。
 */
@RestController
@RequestMapping("/api/callback/wechat-pay")
@Tag(name = "微信支付回调")
public class WechatPaymentCallbackController {
    private final PersistentOrderService persistentOrderService;
    public WechatPaymentCallbackController(PersistentOrderService persistentOrderService) { this.persistentOrderService = persistentOrderService; }
    @Operation(summary = "处理商品订单支付回调")
    @PostMapping
    public PersistentOrderService.PersistentOrderResult callback(@Valid @RequestBody PaymentCallbackRequest request) {
        return persistentOrderService.confirmPayment(request.orderNo(), request.transactionId(), request.amountCent(), request.signature());
    }
}
