package com.tandiantong.framework.web.config;

import com.tandiantong.framework.common.context.ThreadLocalContextCleaner;
import com.tandiantong.framework.web.core.filter.RequestContextCleanupFilter;
import com.tandiantong.framework.web.core.filter.TraceIdFilter;
import com.tandiantong.framework.web.core.handler.ApiResponseAdvice;
import com.tandiantong.framework.web.core.handler.GlobalExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/** Web 层通用自动配置。 */
@AutoConfiguration
public class TandiantongWebAutoConfiguration {

    @Bean
    public TraceIdFilter traceIdFilter() {
        return new TraceIdFilter();
    }

    @Bean
    public RequestContextCleanupFilter requestContextCleanupFilter(ObjectProvider<ThreadLocalContextCleaner> cleaners) {
        return new RequestContextCleanupFilter(cleaners);
    }

    @Bean
    public ApiResponseAdvice apiResponseAdvice() {
        return new ApiResponseAdvice();
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
