package com.tandiantong.integration.wechatpay;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** 本地开发使用的微信支付替身实现。 */
public class LocalWechatPayClient implements WechatPayClient {

    private final String localSecret;

    public LocalWechatPayClient(String localSecret) {
        this.localSecret = localSecret;
    }

    @Override
    public WechatPrepayResult createPrepay(String orderNo, int amountCent, String description) {
        return new WechatPrepayResult("LOCAL-PREPAY-" + orderNo, sign(orderNo + "|" + amountCent));
    }

    @Override
    public boolean verifyCallback(String orderNo, String transactionId, int amountCent, String signature) {
        return signCallback(orderNo, transactionId, amountCent).equals(signature);
    }

    @Override
    public WechatRefundResult refund(String orderNo, String refundNo, int amountCent, String reason) {
        return new WechatRefundResult(refundNo, true, "本地模拟退款成功");
    }

    public String signCallback(String orderNo, String transactionId, int amountCent) {
        return sign(orderNo + "|" + transactionId + "|" + amountCent);
    }

    private String sign(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest((raw + "|" + localSecret).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境缺少 SHA-256 摘要算法", exception);
        }
    }
}
