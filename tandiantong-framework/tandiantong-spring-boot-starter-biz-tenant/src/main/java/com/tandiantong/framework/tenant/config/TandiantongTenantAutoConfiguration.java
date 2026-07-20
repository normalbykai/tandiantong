package com.tandiantong.framework.tenant.config;

import com.tandiantong.framework.common.context.ThreadLocalContextCleaner;
import com.tandiantong.framework.tenant.core.context.TenantContextHolder;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/** 租户业务组件自动配置。 */
@AutoConfiguration
public class TandiantongTenantAutoConfiguration {

    @Bean
    public ThreadLocalContextCleaner tenantContextCleaner() {
        return TenantContextHolder::clear;
    }
}
