package com.tandiantong.framework.security.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tandiantong.framework.common.context.ThreadLocalContextCleaner;
import com.tandiantong.framework.security.core.AccessDomain;
import com.tandiantong.framework.security.core.LoginUser;
import com.tandiantong.framework.security.core.context.LoginUserContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class TandiantongSecurityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TandiantongSecurityAutoConfiguration.class));

    @AfterEach
    void tearDown() {
        LoginUserContextHolder.clear();
    }

    @Test
    void shouldRegisterLoginUserContextCleaner() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("loginUserContextCleaner");
            LoginUserContextHolder.set(new LoginUser(
                    10L,
                    AccessDomain.TENANT,
                    100L,
                    1000L,
                    "13900000000",
                    "租户管理员"));

            context.getBean(ThreadLocalContextCleaner.class).clear();

            assertThatThrownBy(LoginUserContextHolder::currentUser)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("当前请求缺少用户上下文");
        });
    }
}
