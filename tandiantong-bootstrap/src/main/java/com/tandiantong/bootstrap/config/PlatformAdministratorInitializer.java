package com.tandiantong.bootstrap.config;

import com.tandiantong.security.auth.PlatformAdministratorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 平台管理员初始化配置，仅在显式提供本地初始化账号时执行。
 */
@Configuration
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class PlatformAdministratorInitializer {

    @Bean
    public CommandLineRunner initializePlatformAdministrator(PlatformAdministratorService platformAdministratorService,
                                                               org.springframework.core.env.Environment environment) {
        return arguments -> {
            String mobile = environment.getProperty("TDT_BOOTSTRAP_PLATFORM_MOBILE");
            String password = environment.getProperty("TDT_BOOTSTRAP_PLATFORM_PASSWORD");
            String name = environment.getProperty("TDT_BOOTSTRAP_PLATFORM_NAME", "本地平台管理员");
            if (mobile == null || password == null || mobile.isBlank() || password.isBlank()) {
                return;
            }
            platformAdministratorService.createIfAbsent(mobile, password, name);
        };
    }
}
