package com.tandiantong.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tandiantong.security.context.AccessDomain;
import org.junit.jupiter.api.Test;

class PasswordAndTokenServiceTest {

    @Test
    void passwordHashShouldNotExposePlainText() {
        PasswordService passwordService = new PasswordService();

        String hash = passwordService.hash("S3curePwd!");

        assertThat(hash).isNotEqualTo("S3curePwd!");
        assertThat(passwordService.matches("S3curePwd!", hash)).isTrue();
        assertThat(passwordService.matches("wrong", hash)).isFalse();
    }

    @Test
    void tokenShouldCarryUserIdAndAccessDomain() {
        TokenService tokenService = new TokenService("0123456789abcdef0123456789abcdef", 3600);

        String token = tokenService.issue(12L, AccessDomain.TENANT, 3);
        AuthTokenClaims claims = tokenService.parse(token);

        assertThat(claims.userId()).isEqualTo(12L);
        assertThat(claims.domain()).isEqualTo(AccessDomain.TENANT);
        assertThat(claims.tokenVersion()).isEqualTo(3);
    }

    @Test
    void tokenShouldRejectWrongExpectedDomain() {
        TokenService tokenService = new TokenService("0123456789abcdef0123456789abcdef", 3600);

        String token = tokenService.issue(1L, AccessDomain.PLATFORM, 1);

        assertThatThrownBy(() -> tokenService.parseForDomain(token, AccessDomain.TENANT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("令牌权限域不匹配");
    }
}
