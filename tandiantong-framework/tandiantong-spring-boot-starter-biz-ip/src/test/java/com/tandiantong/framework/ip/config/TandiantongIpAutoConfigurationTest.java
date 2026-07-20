package com.tandiantong.framework.ip.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.tandiantong.framework.ip.core.ClientIpResolver;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class TandiantongIpAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TandiantongIpAutoConfiguration.class));

    @Test
    void shouldRegisterClientIpResolver() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(ClientIpResolver.class));
    }
}
