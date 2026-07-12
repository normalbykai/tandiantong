package com.tandiantong.security.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SecurityContextHolderTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clear();
    }

    @Test
    void platformContextShouldNotRequireTenant() {
        CurrentUser user = CurrentUser.platform(1L, "13800000000", "平台管理员");

        SecurityContextHolder.set(user);

        assertThat(SecurityContextHolder.currentUser()).isEqualTo(user);
        assertThat(SecurityContextHolder.currentUser().domain()).isEqualTo(AccessDomain.PLATFORM);
        assertThat(SecurityContextHolder.currentUser().tenantIdOptional()).isEmpty();
    }

    @Test
    void tenantContextShouldRequireTenantAndStore() {
        CurrentUser user = CurrentUser.tenant(10L, 100L, 1000L, "13900000000", "租户管理员");

        SecurityContextHolder.set(user);

        assertThat(SecurityContextHolder.currentUser().domain()).isEqualTo(AccessDomain.TENANT);
        assertThat(SecurityContextHolder.currentTenantId()).isEqualTo(100L);
        assertThat(SecurityContextHolder.currentStoreId()).isEqualTo(1000L);
    }

    @Test
    void tenantFactoryShouldRejectMissingTenant() {
        assertThatThrownBy(() -> CurrentUser.tenant(10L, null, 1000L, "13900000000", "租户管理员"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("租户用户必须包含租户和门店上下文");
    }

    @Test
    void currentUserShouldFailWhenContextMissing() {
        assertThatThrownBy(SecurityContextHolder::currentUser)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("当前请求缺少用户上下文");
    }

    @Test
    void clearShouldRemoveContext() {
        SecurityContextHolder.set(CurrentUser.platform(1L, "13800000000", "平台管理员"));

        SecurityContextHolder.clear();

        assertThatThrownBy(SecurityContextHolder::currentUser)
                .isInstanceOf(IllegalStateException.class);
    }
}
