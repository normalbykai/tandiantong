package com.tandiantong.bootstrap.config;

import com.tandiantong.security.auth.PasswordService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class PlatformAdministratorInitializer {

    @Bean
    public CommandLineRunner initializePlatformAdministrator(JdbcTemplate jdbcTemplate,
                                                               org.springframework.core.env.Environment environment) {
        return arguments -> {
            String mobile = environment.getProperty("TDT_BOOTSTRAP_PLATFORM_MOBILE");
            String password = environment.getProperty("TDT_BOOTSTRAP_PLATFORM_PASSWORD");
            String name = environment.getProperty("TDT_BOOTSTRAP_PLATFORM_NAME", "本地平台管理员");
            if (mobile == null || password == null || mobile.isBlank() || password.isBlank()) {
                return;
            }
            Integer count = jdbcTemplate.queryForObject("select count(*) from platform_user where mobile = ?", Integer.class, mobile);
            if (count != null && count == 0) {
                jdbcTemplate.update("insert into platform_user (id, mobile, display_name, password_hash, status, token_version) values (?, ?, ?, ?, ?, 1)",
                        System.currentTimeMillis(), mobile, name, new PasswordService().hash(password), "ENABLED");
            }
        };
    }
}
