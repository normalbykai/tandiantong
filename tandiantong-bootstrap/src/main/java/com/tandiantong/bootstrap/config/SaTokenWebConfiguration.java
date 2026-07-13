package com.tandiantong.bootstrap.config;

import com.tandiantong.bootstrap.security.SaTokenAuthenticationInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token Web 鉴权配置。
 */
@Configuration
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class SaTokenWebConfiguration implements WebMvcConfigurer {

    private final SaTokenAuthenticationInterceptor saTokenAuthenticationInterceptor;

    public SaTokenWebConfiguration(SaTokenAuthenticationInterceptor saTokenAuthenticationInterceptor) {
        this.saTokenAuthenticationInterceptor = saTokenAuthenticationInterceptor;
    }

    /**
     * 对所有请求注册 Sa-Token 认证拦截器，由拦截器内部按接口分区决定是否校验。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(saTokenAuthenticationInterceptor).addPathPatterns("/**");
    }
}
