package com.tandiantong.framework.web.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.tandiantong.framework.common.context.ThreadLocalContextCleaner;
import com.tandiantong.framework.web.core.filter.RequestContextCleanupFilter;
import com.tandiantong.framework.web.core.filter.TraceIdFilter;
import com.tandiantong.framework.web.core.handler.ApiResponseAdvice;
import com.tandiantong.framework.web.core.handler.GlobalExceptionHandler;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TandiantongWebAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TandiantongWebAutoConfiguration.class));

    @Test
    void shouldRegisterWebInfrastructureBeans() {
        contextRunner.run(context -> assertThat(context)
                .hasSingleBean(TraceIdFilter.class)
                .hasSingleBean(RequestContextCleanupFilter.class)
                .hasSingleBean(ApiResponseAdvice.class)
                .hasSingleBean(GlobalExceptionHandler.class));
    }

    @Test
    void cleanupFilterShouldRunRegisteredCleaners() {
        AtomicBoolean firstCleaned = new AtomicBoolean(false);
        AtomicBoolean secondCleaned = new AtomicBoolean(false);
        contextRunner
                .withBean("firstCleaner", ThreadLocalContextCleaner.class, () -> () -> firstCleaned.set(true))
                .withBean("secondCleaner", ThreadLocalContextCleaner.class, () -> () -> secondCleaned.set(true))
                .run(context -> {
                    doFilter(context.getBean(RequestContextCleanupFilter.class));

                    assertThat(firstCleaned).isTrue();
                    assertThat(secondCleaned).isTrue();
                    assertThat(context).getBeans(ThreadLocalContextCleaner.class).hasSize(2);
                });
    }

    private void doFilter(RequestContextCleanupFilter filter) throws ServletException, IOException {
        filter.doFilter(
                new MockHttpServletRequest("GET", "/api/admin/v1/orders"),
                new MockHttpServletResponse(),
                new MockFilterChain());
    }
}
