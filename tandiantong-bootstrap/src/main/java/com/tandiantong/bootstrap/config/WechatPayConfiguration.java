package com.tandiantong.bootstrap.config;

import com.tandiantong.integration.wechatpay.LocalWechatPayClient;
import com.tandiantong.integration.wechatpay.WechatPayClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WechatPayConfiguration {

    @Bean
    public WechatPayClient wechatPayClient(@Value("${TDT_LOCAL_WECHAT_PAY_SECRET:local-wechat-pay-secret}") String secret) {
        return new LocalWechatPayClient(secret);
    }
}
