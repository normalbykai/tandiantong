package com.tandiantong.framework.operatelog.config;

import com.tandiantong.framework.ip.core.ClientIpResolver;
import com.tandiantong.framework.operatelog.core.service.OperationLogRequestEnricher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

/** 操作日志组件自动配置。 */
@AutoConfiguration
public class TandiantongOperateLogAutoConfiguration {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public OperationLogRequestEnricher operationLogRequestEnricher(
            ClientIpResolver clientIpResolver,
            ObjectProvider<HttpServletRequest> requestProvider) {
        return new OperationLogRequestEnricher(clientIpResolver, requestProvider);
    }
}
