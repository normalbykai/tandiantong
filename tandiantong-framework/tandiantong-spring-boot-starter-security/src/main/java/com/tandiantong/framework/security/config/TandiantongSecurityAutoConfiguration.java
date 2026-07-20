package com.tandiantong.framework.security.config;

import com.tandiantong.framework.common.context.ThreadLocalContextCleaner;
import com.tandiantong.framework.security.core.context.LoginUserContextHolder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/** 安全框架组件自动配置。 */
@AutoConfiguration
public class TandiantongSecurityAutoConfiguration {

    @Bean
    public ThreadLocalContextCleaner loginUserContextCleaner() {
        return LoginUserContextHolder::clear;
    }
}
