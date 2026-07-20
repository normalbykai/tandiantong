package com.tandiantong.security.rbac;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tandiantong.security.context.AccessDomain;
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
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/** 从数据库角色关系判定当前用户接口权限的服务。 */
@Service
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class PermissionAuthorizationService {

    private static final String ENABLED_STATUS = "ENABLED";
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;

    public PermissionAuthorizationService(UserRoleMapper userRoleMapper, RoleMapper roleMapper,
                                          RolePermissionMapper rolePermissionMapper, PermissionMapper permissionMapper) {
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.permissionMapper = permissionMapper;
    }

    /** 判断当前用户是否拥有指定接口权限，并始终在权限域和租户范围内查询关系。 */
    public boolean hasPermission(CurrentUser currentUser, String permissionCode) {
        return listPermissionCodes(currentUser.domain(), currentUser.tenantId(), currentUser.userId()).contains(permissionCode);
    }

    /** 返回用户在指定权限域和租户范围内拥有的全部权限编码，供 Sa-Token 原生鉴权调用。 */
    public List<String> listPermissionCodes(AccessDomain domain, Long tenantId, Long userId) {
        if (domain == AccessDomain.TENANT && tenantId == null) {
            return List.of();
        }
        List<UserRoleEntity> userRoles = userRoleMapper.selectList(new LambdaQueryWrapper<UserRoleEntity>()
                .eq(UserRoleEntity::getDomain, domain.name())
                .eq(UserRoleEntity::getUserId, userId)
                .eq(domain == AccessDomain.TENANT, UserRoleEntity::getTenantId, tenantId)
                .isNull(domain == AccessDomain.PLATFORM, UserRoleEntity::getTenantId));
        Set<Long> roleIds = userRoles.stream().map(UserRoleEntity::getRoleId).collect(java.util.stream.Collectors.toSet());
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<RoleEntity> roles = roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getDomain, domain.name())
                .in(RoleEntity::getId, roleIds)
                .eq(RoleEntity::getStatus, ENABLED_STATUS)
                .eq(domain == AccessDomain.TENANT, RoleEntity::getTenantId, tenantId)
                .isNull(domain == AccessDomain.PLATFORM, RoleEntity::getTenantId));
        Set<Long> verifiedRoleIds = roles.stream().map(RoleEntity::getId).collect(java.util.stream.Collectors.toSet());
        Set<String> permissionCodes = new HashSet<>();
        if (!verifiedRoleIds.isEmpty()) {
            List<RolePermissionEntity> rolePermissions = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermissionEntity>()
                    .eq(RolePermissionEntity::getDomain, domain.name())
                    .in(RolePermissionEntity::getRoleId, verifiedRoleIds)
                    .eq(domain == AccessDomain.TENANT, RolePermissionEntity::getTenantId, tenantId)
                    .isNull(domain == AccessDomain.PLATFORM, RolePermissionEntity::getTenantId));
            Set<Long> permissionIds = rolePermissions.stream().map(RolePermissionEntity::getPermissionId)
                    .collect(java.util.stream.Collectors.toSet());
            if (!permissionIds.isEmpty()) {
                permissionCodes.addAll(permissionMapper.selectList(new LambdaQueryWrapper<PermissionEntity>()
                        .eq(PermissionEntity::getDomain, domain.name())
                        .in(PermissionEntity::getId, permissionIds)).stream()
                        .map(PermissionEntity::getPermissionCode)
                        .toList());
            }
        }
        return List.copyOf(permissionCodes);
    }
}
