package com.tandiantong.security.rbac;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.entity.PermissionEntity;
import com.tandiantong.security.entity.RoleEntity;
import com.tandiantong.security.entity.RolePermissionEntity;
import com.tandiantong.security.entity.UserRoleEntity;
import com.tandiantong.security.mapper.PermissionMapper;
import com.tandiantong.security.mapper.RoleMapper;
import com.tandiantong.security.mapper.RolePermissionMapper;
import com.tandiantong.security.mapper.UserRoleMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

/** 数据库权限判定服务测试。 */
class PermissionAuthorizationServiceTest {

    @Test
    void tenantUserShouldOnlyReceivePermissionsFromOwnTenantRoles() {
        UserRoleMapper userRoleMapper = mock(UserRoleMapper.class);
        RoleMapper roleMapper = mock(RoleMapper.class);
        RolePermissionMapper rolePermissionMapper = mock(RolePermissionMapper.class);
        PermissionMapper permissionMapper = mock(PermissionMapper.class);
        PermissionAuthorizationService service = new PermissionAuthorizationService(
                userRoleMapper, roleMapper, rolePermissionMapper, permissionMapper);

        UserRoleEntity userRole = new UserRoleEntity();
        userRole.setRoleId(11L);
        RoleEntity role = new RoleEntity();
        role.setId(11L);
        RolePermissionEntity rolePermission = new RolePermissionEntity();
        rolePermission.setPermissionId(101L);
        when(userRoleMapper.selectList(any())).thenReturn(List.of(userRole));
        when(roleMapper.selectList(any())).thenReturn(List.of(role));
        when(rolePermissionMapper.selectList(any())).thenReturn(List.of(rolePermission));
        PermissionEntity permission = new PermissionEntity();
        permission.setPermissionCode("order:refund:create");
        when(permissionMapper.selectList(any())).thenReturn(List.of(permission));

        assertThat(service.hasPermission(CurrentUser.tenant(1L, 100L, 1001L, "13800000000", "甲商户用户"),
                "order:refund:create")).isTrue();

        when(roleMapper.selectList(any())).thenReturn(List.of());
        assertThat(service.hasPermission(CurrentUser.tenant(2L, 200L, 2001L, "13900000000", "乙商户用户"),
                "order:refund:create")).isFalse();

    }
}
