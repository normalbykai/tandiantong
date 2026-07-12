package com.tandiantong.integration.wechatpay;

public record WechatRefundResult(String refundNo, boolean success, String message) {
}
