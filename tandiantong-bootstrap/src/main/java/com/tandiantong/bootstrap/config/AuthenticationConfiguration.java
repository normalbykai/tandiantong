package com.tandiantong.bootstrap.config;

import com.tandiantong.security.auth.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class AuthenticationConfiguration {

    @Bean
    public TokenService tokenService(@Value("${TDT_JWT_SECRET:local-development-secret-must-be-replaced-2026}") String secret) {
        return new TokenService(secret, 3600);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager();
    }
}
