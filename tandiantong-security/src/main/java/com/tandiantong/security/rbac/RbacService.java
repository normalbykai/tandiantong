package com.tandiantong.security.rbac;

import com.tandiantong.security.context.AccessDomain;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RbacService {

    private final Map<Long, Set<String>> platformRolePermissions = new HashMap<>();
    private final Map<Long, Set<Long>> platformUserRoles = new HashMap<>();
    private final Map<TenantRoleKey, Set<String>> tenantRolePermissions = new HashMap<>();
    private final Map<TenantUserKey, Set<Long>> tenantUserRoles = new HashMap<>();

    public void grantPlatformRole(Long roleId, String roleName, Set<String> permissions) {
        platformRolePermissions.put(roleId, Set.copyOf(permissions));
    }

    public void assignPlatformRole(Long userId, Long roleId) {
        platformUserRoles.computeIfAbsent(userId, ignored -> new HashSet<>()).add(roleId);
    }

    public void grantTenantRole(Long tenantId, Long roleId, String roleName, Set<String> permissions) {
        Set<String> apiPermissions = new HashSet<>(permissions);
        apiPermissions.removeIf(permission -> permission.startsWith("platform:"));
        tenantRolePermissions.put(new TenantRoleKey(tenantId, roleId), Set.copyOf(apiPermissions));
    }

    public void assignTenantRole(Long tenantId, Long userId, Long roleId) {
        tenantUserRoles.computeIfAbsent(new TenantUserKey(tenantId, userId), ignored -> new HashSet<>()).add(roleId);
    }

    public boolean hasApiPermission(AccessDomain domain, Long tenantId, Long userId, String permissionCode) {
        if (domain == AccessDomain.PLATFORM) {
            return platformUserRoles.getOrDefault(userId, Set.of()).stream()
                    .flatMap(roleId -> platformRolePermissions.getOrDefault(roleId, Set.of()).stream())
                    .anyMatch(permissionCode::equals);
        }
        if (tenantId == null) {
            return false;
        }
        return tenantUserRoles.getOrDefault(new TenantUserKey(tenantId, userId), Set.of()).stream()
                .flatMap(roleId -> tenantRolePermissions.getOrDefault(new TenantRoleKey(tenantId, roleId), Set.of()).stream())
                .anyMatch(permissionCode::equals);
    }

    private record TenantRoleKey(Long tenantId, Long roleId) {

        private TenantRoleKey {
            Objects.requireNonNull(tenantId, "tenantId");
            Objects.requireNonNull(roleId, "roleId");
        }
    }

    private record TenantUserKey(Long tenantId, Long userId) {

        private TenantUserKey {
            Objects.requireNonNull(tenantId, "tenantId");
            Objects.requireNonNull(userId, "userId");
        }
    }
}
