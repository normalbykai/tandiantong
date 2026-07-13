package com.tandiantong.bootstrap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/** 测试与兼容场景使用的 Spring Security 基础认证配置。 */
@Configuration
public class AuthenticationConfiguration {

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}
