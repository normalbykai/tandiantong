package com.tandiantong.integration.wechatpay;

/** 微信支付适配接口，隔离业务模块与第三方支付实现。 */
public interface WechatPayClient {

    WechatPrepayResult createPrepay(String orderNo, int amountCent, String description);

    boolean verifyCallback(String orderNo, String transactionId, int amountCent, String signature);

    WechatRefundResult refund(String orderNo, String refundNo, int amountCent, String reason);
}
