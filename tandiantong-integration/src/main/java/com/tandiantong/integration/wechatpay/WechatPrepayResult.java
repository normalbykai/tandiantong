package com.tandiantong.integration.wechatpay;

/** 微信预支付创建结果。 */
public record WechatPrepayResult(String prepayId, String payNonce) {
}
