package com.tandiantong.framework.operatelog.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.tandiantong.framework.ip.config.TandiantongIpAutoConfiguration;
import com.tandiantong.framework.ip.core.ClientIpResolver;
import com.tandiantong.framework.operatelog.core.service.OperationLogRequestEnricher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

class TandiantongOperateLogAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    TandiantongIpAutoConfiguration.class,
                    TandiantongOperateLogAutoConfiguration.class));

    @Test
    void shouldRegisterOperationLogRequestEnricher() {
        contextRunner.run(context -> assertThat(context)
                .hasSingleBean(ClientIpResolver.class)
                .hasSingleBean(OperationLogRequestEnricher.class));
    }
}
