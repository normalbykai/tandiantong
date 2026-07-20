package com.tandiantong.framework.ip.config;

import com.tandiantong.framework.ip.core.ClientIpResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/** IP 业务组件自动配置。 */
@AutoConfiguration
public class TandiantongIpAutoConfiguration {

    @Bean
    public ClientIpResolver clientIpResolver() {
        return new ClientIpResolver();
    }
}
