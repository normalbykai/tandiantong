package com.tandiantong.security.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.security.context.AccessDomain;
import org.junit.jupiter.api.Test;

class PasswordAndSaTokenLoginIdTest {

    @Test
    void passwordHashShouldNotExposePlainText() {
        PasswordService passwordService = new PasswordService();

        String hash = passwordService.hash("S3curePwd!");

        assertThat(hash).isNotEqualTo("S3curePwd!");
        assertThat(passwordService.matches("S3curePwd!", hash)).isTrue();
        assertThat(passwordService.matches("wrong", hash)).isFalse();
    }

    @Test
    void saTokenLoginIdShouldCarryUserIdAndAccessDomain() {
        SaTokenLoginId loginId = new SaTokenLoginId(AccessDomain.TENANT, 12L);

        SaTokenLoginId parsed = SaTokenLoginId.parse(loginId.encode());

        assertThat(parsed.userId()).isEqualTo(12L);
        assertThat(parsed.domain()).isEqualTo(AccessDomain.TENANT);
    }

    @Test
    void saTokenLoginIdShouldRejectInvalidValue() {
        assertThatThrownBy(() -> SaTokenLoginId.parse("invalid"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("登录状态无效或已过期");
    }
}
