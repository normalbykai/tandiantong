package com.tandiantong.security.rbac;

import static org.assertj.core.api.Assertions.assertThat;

import com.tandiantong.security.context.AccessDomain;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RbacServiceTest {

    @Test
    void sameRoleNameAcrossTenantsShouldNotSharePermissions() {
        RbacService rbacService = new RbacService();
        rbacService.grantTenantRole(100L, 10L, "运营人员", Set.of("catalog:product:read"));
        rbacService.grantTenantRole(200L, 20L, "运营人员", Set.of("order:refund:create"));
        rbacService.assignTenantRole(100L, 1L, 10L);
        rbacService.assignTenantRole(200L, 2L, 20L);

        assertThat(rbacService.hasApiPermission(AccessDomain.TENANT, 100L, 1L, "catalog:product:read")).isTrue();
        assertThat(rbacService.hasApiPermission(AccessDomain.TENANT, 100L, 1L, "order:refund:create")).isFalse();
        assertThat(rbacService.hasApiPermission(AccessDomain.TENANT, 200L, 2L, "order:refund:create")).isTrue();
        assertThat(rbacService.hasApiPermission(AccessDomain.TENANT, 200L, 2L, "catalog:product:read")).isFalse();
    }

    @Test
    void platformAndTenantPermissionsShouldBeSeparated() {
        RbacService rbacService = new RbacService();
        rbacService.grantPlatformRole(1L, "平台运营", Set.of("platform:tenant:create"));
        rbacService.assignPlatformRole(88L, 1L);
        rbacService.grantTenantRole(100L, 10L, "租户管理员", Set.of("platform:tenant:create"));
        rbacService.assignTenantRole(100L, 88L, 10L);

        assertThat(rbacService.hasApiPermission(AccessDomain.PLATFORM, null, 88L, "platform:tenant:create")).isTrue();
        assertThat(rbacService.hasApiPermission(AccessDomain.TENANT, 100L, 88L, "platform:tenant:create")).isFalse();
    }
}
