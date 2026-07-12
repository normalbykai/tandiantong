package com.tandiantong.integration.wechatpay;

public interface WechatPayClient {

    WechatPrepayResult createPrepay(String orderNo, int amountCent, String description);

    boolean verifyCallback(String orderNo, String transactionId, int amountCent, String signature);

    WechatRefundResult refund(String orderNo, String refundNo, int amountCent, String reason);
}
