package com.tandiantong.integration.wechatpay;

/** 微信退款调用结果。 */
public record WechatRefundResult(String refundNo, boolean success, String message) {
}
