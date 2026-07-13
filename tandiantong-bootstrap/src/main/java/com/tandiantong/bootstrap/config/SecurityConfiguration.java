package com.tandiantong.bootstrap.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 兼容配置，生产鉴权由 Sa-Token 拦截器承担。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain saTokenSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "false")
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/platform/v1/health", "/api/callback/**", "/actuator/health",
                                "/foundation-test/**").permitAll()
                        .requestMatchers("/api/mini/v1/catalog/products").permitAll()
                        .requestMatchers("/api/mini/v1/orders", "/api/callback/wechat-pay").permitAll()
                        .requestMatchers("/api/mini/v1/reservations", "/api/mini/v1/reservations/services").permitAll()
                        .requestMatchers("/api/platform/v1/auth/login", "/api/admin/v1/auth/login", "/api/admin/v1/auth/activate").permitAll()
                        .requestMatchers("/api/platform/v1/**").hasRole("PLATFORM")
                        .requestMatchers("/api/admin/v1/**").hasRole("TENANT")
                        .anyRequest().authenticated());
        return http.build();
    }
}
