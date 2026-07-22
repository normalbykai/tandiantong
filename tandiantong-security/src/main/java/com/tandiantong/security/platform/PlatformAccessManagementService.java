package com.tandiantong.security.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tandiantong.framework.common.api.ErrorCode;
import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.security.audit.OperationAuditService;
import com.tandiantong.security.audit.AuditAction;
import com.tandiantong.security.audit.AuditEvent;
import com.tandiantong.security.audit.AuditTarget;
import com.tandiantong.security.auth.PasswordService;
import com.tandiantong.security.context.AccessDomain;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.entity.PermissionEntity;
import com.tandiantong.security.entity.PlatformUserEntity;
import com.tandiantong.security.entity.RoleEntity;
import com.tandiantong.security.entity.RolePermissionEntity;
import com.tandiantong.security.entity.UserRoleEntity;
import com.tandiantong.security.mapper.PermissionMapper;
import com.tandiantong.security.mapper.PlatformUserMapper;
import com.tandiantong.security.mapper.RoleMapper;
import com.tandiantong.security.mapper.RolePermissionMapper;
import com.tandiantong.security.mapper.UserRoleMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/** 平台权限域账号、角色和权限点管理服务。 */
@Service
@ConditionalOnProperty(
        prefix = "tandiantong.security",
        name = "database-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PlatformAccessManagementService {
    private static final String DOMAIN = AccessDomain.PLATFORM.name();
    private static final String ENABLED = "ENABLED";
    private static final String DISABLED = "DISABLED";
    private final PlatformUserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PasswordService passwordService;
    private final OperationAuditService auditService;
    private final PlatformSystemManagementService systemManagementService;

    public PlatformAccessManagementService(
            PlatformUserMapper userMapper,
            RoleMapper roleMapper,
            PermissionMapper permissionMapper,
            UserRoleMapper userRoleMapper,
            RolePermissionMapper rolePermissionMapper,
            PasswordService passwordService,
            OperationAuditService auditService,
            PlatformSystemManagementService systemManagementService) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.passwordService = passwordService;
        this.auditService = auditService;
        this.systemManagementService = systemManagementService;
    }

    public List<PlatformUserEntity> listAccounts() {
        return userMapper.selectList(
                new LambdaQueryWrapper<PlatformUserEntity>()
                        .orderByDesc(PlatformUserEntity::getId)
                        .last("limit 200"));
    }

    public List<RoleEntity> listRoles() {
        return roleMapper.selectList(platformRoleQuery().orderByAsc(RoleEntity::getId));
    }

    public PlatformPermissionPage listPermissions(
            String keyword, String permissionType, long page, long pageSize) {
        if (page < 1) {
            throw error("页码必须大于0");
        }
        if (pageSize < 1 || pageSize > 20) {
            throw error("每页条数必须介于1到20之间");
        }
        LambdaQueryWrapper<PermissionEntity> query = new LambdaQueryWrapper<PermissionEntity>()
                .eq(PermissionEntity::getDomain, DOMAIN)
                .orderByAsc(PermissionEntity::getPermissionCode);
        if (keyword != null && !keyword.isBlank()) {
            String trimmedKeyword = keyword.trim();
            query.and(wrapper -> wrapper
                    .like(PermissionEntity::getName, trimmedKeyword)
                    .or()
                    .like(PermissionEntity::getPermissionCode, trimmedKeyword));
        }
        if (permissionType != null && !permissionType.isBlank()) {
            query.eq(PermissionEntity::getPermissionType, permissionType.trim());
        }
        Page<PermissionEntity> result = permissionMapper.selectPage(new Page<>(page, pageSize), query);
        return new PlatformPermissionPage(
                result.getTotal(), result.getCurrent(), result.getSize(), result.getRecords());
    }

    public List<PermissionEntity> listPermissionOptions() {
        return permissionMapper.selectList(
                new LambdaQueryWrapper<PermissionEntity>()
                        .eq(PermissionEntity::getDomain, DOMAIN)
                        .orderByAsc(PermissionEntity::getPermissionCode));
    }

    public List<Long> listRoleIds(Long userId) {
        return userRoleMapper
                .selectList(platformUserRoleQuery().eq(UserRoleEntity::getUserId, userId))
                .stream()
                .map(UserRoleEntity::getRoleId)
                .toList();
    }

    public List<Long> listPermissionIds(Long roleId) {
        requirePlatformRole(roleId);
        return rolePermissionMapper
                .selectList(
                        platformRolePermissionQuery().eq(RolePermissionEntity::getRoleId, roleId))
                .stream()
                .map(RolePermissionEntity::getPermissionId)
                .toList();
    }

    /** 平台权限管理页面的分页结果。 */
    public static class PlatformPermissionPage {
        private final long total;
        private final long current;
        private final long pageSize;
        private final List<PermissionEntity> records;

        public PlatformPermissionPage(long total, long current, long pageSize, List<PermissionEntity> records) {
            this.total = total;
            this.current = current;
            this.pageSize = pageSize;
            this.records = List.copyOf(records);
        }

        public long total() { return total; }
        public long current() { return current; }
        public long pageSize() { return pageSize; }
        public List<PermissionEntity> records() { return records; }
    }

    @Transactional
    public PlatformUserEntity createAccount(
            CurrentUser operator,
            String mobile,
            String displayName,
            String password,
            List<Long> roleIds) {
        if (userMapper.selectOne(
                        new LambdaQueryWrapper<PlatformUserEntity>()
                                .eq(PlatformUserEntity::getMobile, mobile))
                != null) throw error("手机号已被平台注册");
        systemManagementService.validatePlatformPassword(password);
        verifyAssignableRoles(operator, roleIds);
        PlatformUserEntity user = new PlatformUserEntity();
        user.setMobile(mobile);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordService.hash(password));
        user.setStatus(ENABLED);
        user.setTokenVersion(1);
        userMapper.insert(user);
        replaceUserRoles(user.getId(), roleIds);
        auditService.record(
                operator, AuditEvent.of(AuditAction.PLATFORM_ACCOUNT_CREATED, AuditTarget.of("平台账号", user.getId(), displayName)));
        return user;
    }

    @Transactional
    public void updateAccount(
            CurrentUser operator, Long userId, String displayName, List<Long> roleIds) {
        PlatformUserEntity user = requireAccount(userId);
        verifyAssignableRoles(operator, roleIds);
        user.setDisplayName(displayName);
        userMapper.updateById(user);
        replaceUserRoles(userId, roleIds);
        auditService.record(
                operator, AuditEvent.of(AuditAction.PLATFORM_ACCOUNT_UPDATED, AuditTarget.of("平台账号", userId, user.getDisplayName())));
    }

    @Transactional
    public void updateAccountStatus(CurrentUser operator, Long userId, boolean enabled) {
        if (operator.userId().equals(userId) && !enabled) throw error("不能停用当前登录账号");
        PlatformUserEntity user = requireAccount(userId);
        user.setStatus(enabled ? ENABLED : DISABLED);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userMapper.updateById(user);
        auditService.record(
                operator,
                AuditEvent.of(
                        enabled ? AuditAction.PLATFORM_ACCOUNT_ENABLED : AuditAction.PLATFORM_ACCOUNT_DISABLED,
                        AuditTarget.of("平台账号", userId, user.getDisplayName())));
    }

    @Transactional
    public void unlockAccount(CurrentUser operator, Long userId) {
        PlatformUserEntity user = requireAccount(userId);
        // 显式更新空值，避免 MyBatis-Plus 按默认策略忽略 locked_until 的清空操作。
        int updated = userMapper.update(
                null,
                new LambdaUpdateWrapper<PlatformUserEntity>()
                        .eq(PlatformUserEntity::getId, userId)
                        .set(PlatformUserEntity::getFailedLoginCount, 0)
                        .set(PlatformUserEntity::getLockedUntil, null));
        if (updated != 1) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "平台账号不存在或已被删除");
        }
        auditService.record(
                operator,
                AuditEvent.of(
                        AuditAction.PLATFORM_ACCOUNT_UNLOCKED,
                        AuditTarget.of("平台账号", userId, user.getDisplayName())));
    }

    @Transactional
    public PlatformSystemManagementService.TemporaryPassword resetPassword(
            CurrentUser operator, Long userId) {
        PlatformUserEntity user = requireAccount(userId);
        PlatformSystemManagementService.TemporaryPassword password =
                systemManagementService.resolveResetPassword();
        user.setPasswordHash(password.passwordHash());
        user.setTokenVersion(user.getTokenVersion() + 1);
        userMapper.updateById(user);
        auditService.record(
                operator,
                AuditEvent.of(
                                AuditAction.PLATFORM_ACCOUNT_PASSWORD_RESET,
                                AuditTarget.of("平台账号", userId, user.getDisplayName()))
                        .withPolicy(password.mode()));
        return password;
    }

    @Transactional
    public RoleEntity createRole(
            CurrentUser operator, String name, String roleCode, String description) {
        if (roleMapper.selectOne(platformRoleQuery().eq(RoleEntity::getName, name)) != null)
            throw error("平台角色名称已存在");
        if (roleMapper.selectOne(platformRoleQuery().eq(RoleEntity::getRoleCode, roleCode)) != null)
            throw error("平台角色标识已存在");
        RoleEntity role = new RoleEntity();
        role.setDomain(DOMAIN);
        role.setRoleCode(roleCode);
        role.setName(name);
        role.setDescription(description);
        role.setStatus(ENABLED);
        role.setSystemRole(false);
        roleMapper.insert(role);
        auditService.record(
                operator,
                AuditEvent.of(
                        AuditAction.PLATFORM_ROLE_CREATED,
                        AuditTarget.of("平台角色", role.getId(), name, roleCode)));
        return role;
    }

    @Transactional
    public void updateRole(CurrentUser operator, Long roleId, String name, String description) {
        RoleEntity role = requirePlatformRole(roleId);
        if (!role.getName().equals(name)
                && roleMapper.selectOne(platformRoleQuery().eq(RoleEntity::getName, name)) != null)
            throw error("平台角色名称已存在");
        role.setName(name);
        role.setDescription(description);
        roleMapper.updateById(role);
        auditService.record(
                operator,
                AuditEvent.of(
                        AuditAction.PLATFORM_ROLE_UPDATED,
                        AuditTarget.of("平台角色", roleId, role.getName(), role.getRoleCode())));
    }

    @Transactional
    public void updateRoleStatus(CurrentUser operator, Long roleId, boolean enabled) {
        RoleEntity role = requirePlatformRole(roleId);
        role.setStatus(enabled ? ENABLED : DISABLED);
        roleMapper.updateById(role);
        auditService.record(
                operator,
                AuditEvent.of(
                        enabled ? AuditAction.PLATFORM_ROLE_ENABLED : AuditAction.PLATFORM_ROLE_DISABLED,
                        AuditTarget.of("平台角色", roleId, role.getName(), role.getRoleCode())));
    }

    @Transactional
    public void replaceRolePermissions(
            CurrentUser operator, Long roleId, List<Long> permissionIds) {
        RoleEntity role = requirePlatformRole(roleId);
        verifyAssignablePermissions(operator, permissionIds);
        rolePermissionMapper.delete(
                platformRolePermissionQuery().eq(RolePermissionEntity::getRoleId, roleId));
        for (Long permissionId : Set.copyOf(permissionIds)) {
            RolePermissionEntity relation = new RolePermissionEntity();
            relation.setDomain(DOMAIN);
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            rolePermissionMapper.insert(relation);
        }
        auditService.record(
                operator,
                AuditEvent.of(
                                AuditAction.PLATFORM_ROLE_PERMISSIONS_UPDATED,
                                AuditTarget.of("平台角色", roleId, role.getName(), role.getRoleCode()))
                        .withItemCount(permissionIds.size()));
    }

    private void replaceUserRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(platformUserRoleQuery().eq(UserRoleEntity::getUserId, userId));
        for (Long roleId : Set.copyOf(roleIds)) {
            UserRoleEntity relation = new UserRoleEntity();
            relation.setDomain(DOMAIN);
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            userRoleMapper.insert(relation);
        }
    }

    private void verifyAssignableRoles(CurrentUser operator, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) throw error("平台账号至少需要分配一个角色");
        List<RoleEntity> roles =
                roleMapper.selectList(
                        platformRoleQuery().in(RoleEntity::getId, Set.copyOf(roleIds)));
        if (roles.size() != Set.copyOf(roleIds).size()
                || roles.stream().anyMatch(role -> !ENABLED.equals(role.getStatus())))
            throw error("角色不存在或已停用");
    }

    private void verifyAssignablePermissions(CurrentUser operator, List<Long> permissionIds) {
        if (permissionIds == null) throw error("权限列表不能为空");
        List<PermissionEntity> permissions =
                permissionIds.isEmpty()
                        ? List.of()
                        : permissionMapper.selectList(
                                new LambdaQueryWrapper<PermissionEntity>()
                                        .eq(PermissionEntity::getDomain, DOMAIN)
                                        .in(PermissionEntity::getId, Set.copyOf(permissionIds)));
        if (permissions.size() != Set.copyOf(permissionIds).size()) throw error("权限点不存在或不属于平台权限域");
    }

    private PlatformUserEntity requireAccount(Long id) {
        PlatformUserEntity user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "平台账号不存在");
        return user;
    }

    private RoleEntity requirePlatformRole(Long id) {
        RoleEntity role = roleMapper.selectOne(platformRoleQuery().eq(RoleEntity::getId, id));
        if (role == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "平台角色不存在");
        return role;
    }

    private LambdaQueryWrapper<RoleEntity> platformRoleQuery() {
        return new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getDomain, DOMAIN)
                .isNull(RoleEntity::getTenantId);
    }

    private LambdaQueryWrapper<UserRoleEntity> platformUserRoleQuery() {
        return new LambdaQueryWrapper<UserRoleEntity>()
                .eq(UserRoleEntity::getDomain, DOMAIN)
                .isNull(UserRoleEntity::getTenantId);
    }

    private LambdaQueryWrapper<RolePermissionEntity> platformRolePermissionQuery() {
        return new LambdaQueryWrapper<RolePermissionEntity>()
                .eq(RolePermissionEntity::getDomain, DOMAIN)
                .isNull(RolePermissionEntity::getTenantId);
    }

    private BusinessException error(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
