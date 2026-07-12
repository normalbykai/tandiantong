package com.tandiantong.miniapi.order;

import com.tandiantong.order.app.PersistentOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/callback/wechat-pay")
public class WechatPaymentCallbackController {
    private final PersistentOrderService persistentOrderService;
    public WechatPaymentCallbackController(PersistentOrderService persistentOrderService) { this.persistentOrderService = persistentOrderService; }
    @PostMapping
    public PersistentOrderService.PersistentOrderResult callback(@Valid @RequestBody PaymentCallbackRequest request) {
        return persistentOrderService.confirmPayment(request.orderNo(), request.transactionId(), request.amountCent(), request.signature());
    }
    public record PaymentCallbackRequest(@NotBlank(message="订单号不能为空") String orderNo,@NotBlank(message="支付流水号不能为空") String transactionId,@PositiveOrZero(message="支付金额不能小于零") int amountCent,@NotBlank(message="签名不能为空") String signature) {}
}
