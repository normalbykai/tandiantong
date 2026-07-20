package com.tandiantong.framework.tenant.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tandiantong.framework.common.context.ThreadLocalContextCleaner;
import com.tandiantong.framework.tenant.core.context.TenantContext;
import com.tandiantong.framework.tenant.core.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class TandiantongTenantAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TandiantongTenantAutoConfiguration.class));

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void shouldRegisterTenantContextCleaner() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("tenantContextCleaner");
            TenantContextHolder.set(TenantContext.of(100L, 1000L));

            context.getBean(ThreadLocalContextCleaner.class).clear();

            assertThatThrownBy(TenantContextHolder::current)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("当前请求缺少租户上下文");
        });
    }
}
