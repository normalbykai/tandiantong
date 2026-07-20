package com.tandiantong.framework.mybatis.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.tandiantong.framework.mybatis.core.tenant.TenantIdProvider;
import com.tandiantong.framework.mybatis.core.tenant.TenantTableProvider;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class TandiantongMybatisAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TandiantongMybatisAutoConfiguration.class))
            .withBean(TenantIdProvider.class, () -> () -> 100L)
            .withBean(TenantTableProvider.class, () -> () -> Set.of("sales_order"));

    @Test
    void shouldRegisterMybatisPlusInterceptor() {
        contextRunner.run(context -> assertThat(context)
                .hasSingleBean(TenantIdProvider.class)
                .hasSingleBean(TenantTableProvider.class)
                .hasSingleBean(MybatisPlusInterceptor.class));
    }

    @Test
    void shouldUseBusinessProvidedTenantTableScope() {
        contextRunner.run(context -> {
            TenantLineHandler handler = tenantLineHandler(context.getBean(MybatisPlusInterceptor.class));

            assertThat(handler.ignoreTable("sales_order")).isFalse();
            assertThat(handler.ignoreTable("platform_dictionary_type")).isTrue();
        });
    }

    @Test
    void shouldIgnoreAllTablesWhenTenantTableProviderMissing() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(TandiantongMybatisAutoConfiguration.class))
                .withBean(TenantIdProvider.class, () -> () -> 100L)
                .run(context -> {
                    TenantLineHandler handler = tenantLineHandler(context.getBean(MybatisPlusInterceptor.class));

                    assertThat(handler.ignoreTable("sales_order")).isTrue();
                });
    }

    @SuppressWarnings("unchecked")
    private TenantLineHandler tenantLineHandler(MybatisPlusInterceptor interceptor) throws Exception {
        Field interceptorsField = MybatisPlusInterceptor.class.getDeclaredField("interceptors");
        interceptorsField.setAccessible(true);
        List<InnerInterceptor> innerInterceptors = (List<InnerInterceptor>) interceptorsField.get(interceptor);
        TenantLineInnerInterceptor tenantInterceptor = (TenantLineInnerInterceptor) innerInterceptors.get(0);
        Field tenantLineHandlerField = TenantLineInnerInterceptor.class.getDeclaredField("tenantLineHandler");
        tenantLineHandlerField.setAccessible(true);
        return (TenantLineHandler) tenantLineHandlerField.get(tenantInterceptor);
    }
}
