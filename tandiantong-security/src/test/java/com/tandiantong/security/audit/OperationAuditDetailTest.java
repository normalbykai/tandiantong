package com.tandiantong.security.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tandiantong.security.auth.PasswordService;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.entity.RoleEntity;
import com.tandiantong.security.mapper.PermissionMapper;
import com.tandiantong.security.mapper.PlatformUserMapper;
import com.tandiantong.security.mapper.RoleMapper;
import com.tandiantong.security.mapper.RolePermissionMapper;
import com.tandiantong.security.mapper.UserRoleMapper;
import com.tandiantong.security.platform.PlatformAccessManagementService;
import com.tandiantong.security.platform.PlatformSystemManagementService;

import org.junit.jupiter.api.Test;

class OperationAuditDetailTest {

    @Test
    void shouldRecordRoleNameWhenChangingPlatformRoleStatus() {
        RoleMapper roleMapper = mock(RoleMapper.class);
        RoleEntity role = new RoleEntity();
        role.setId(100005L);
        role.setName("运营专员");
        role.setRoleCode("platform_operator");
        when(roleMapper.selectOne(any())).thenReturn(role);
        OperationAuditService auditService = mock(OperationAuditService.class);
        PlatformAccessManagementService service = new PlatformAccessManagementService(
                mock(PlatformUserMapper.class), roleMapper, mock(PermissionMapper.class),
                mock(UserRoleMapper.class), mock(RolePermissionMapper.class), mock(PasswordService.class),
                auditService, mock(PlatformSystemManagementService.class));

        service.updateRoleStatus(CurrentUser.platform(1L, "13800000000", "平台管理员"), 100005L, true);

        var event = org.mockito.ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditService).record(any(CurrentUser.class), event.capture());
        assertThat(event.getValue().getAction()).isEqualTo(AuditAction.PLATFORM_ROLE_ENABLED);
        assertThat(event.getValue().renderDetail()).isEqualTo("已启用平台角色：运营专员（platform_operator）");
    }
}
