package com.tandiantong.bootstrap.config;

import com.tandiantong.bootstrap.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, org.springframework.beans.factory.ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider) throws Exception {
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
        JwtAuthenticationFilter jwtAuthenticationFilter = jwtAuthenticationFilterProvider.getIfAvailable();
        if (jwtAuthenticationFilter != null) {
            http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }
        return http.build();
    }
}
