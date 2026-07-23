package com.tandiantong.security.tenant;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tandiantong.framework.common.api.ErrorCode;
import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.entity.AdminUserEntity;
import com.tandiantong.security.entity.MerchantSystemConfigEntity;
import com.tandiantong.security.entity.OperationLogEntity;
import com.tandiantong.security.entity.PermissionEntity;
import com.tandiantong.security.entity.RoleEntity;
import com.tandiantong.security.entity.RolePermissionEntity;
import com.tandiantong.security.entity.StoreEntity;
import com.tandiantong.security.entity.UserRoleEntity;
import com.tandiantong.security.audit.AuditAction;
import com.tandiantong.security.audit.AuditEvent;
import com.tandiantong.security.audit.AuditTarget;
import com.tandiantong.security.auth.PasswordService;
import com.tandiantong.security.mapper.AdminUserMapper;
import com.tandiantong.security.mapper.MerchantSystemConfigMapper;
import com.tandiantong.security.mapper.OperationLogMapper;
import com.tandiantong.security.mapper.PermissionMapper;
import com.tandiantong.security.mapper.RoleMapper;
import com.tandiantong.security.mapper.RolePermissionMapper;
import com.tandiantong.security.mapper.StoreMapper;
import com.tandiantong.security.mapper.UserRoleMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 商户基础设施查询服务，所有查询都限定在当前商户和门店范围内。 */
@Service
public class MerchantInfrastructureService {

    private static final String ENABLED_STATUS = "ENABLED";
    private final StoreMapper storeMapper;
    private final AdminUserMapper adminUserMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final OperationLogMapper operationLogMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final MerchantSystemConfigMapper merchantSystemConfigMapper;
    private final com.tandiantong.security.audit.OperationAuditService auditService;
    private final PasswordService passwordService;

    public MerchantInfrastructureService(StoreMapper storeMapper, AdminUserMapper adminUserMapper,
                                         RoleMapper roleMapper, PermissionMapper permissionMapper,
                                         OperationLogMapper operationLogMapper, RolePermissionMapper rolePermissionMapper,
                                         UserRoleMapper userRoleMapper, MerchantSystemConfigMapper merchantSystemConfigMapper,
                                         com.tandiantong.security.audit.OperationAuditService auditService,
                                         PasswordService passwordService) {
        this.storeMapper = storeMapper;
        this.adminUserMapper = adminUserMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.operationLogMapper = operationLogMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.merchantSystemConfigMapper = merchantSystemConfigMapper;
        this.auditService = auditService;
        this.passwordService = passwordService;
    }

    public StoreEntity getStore(CurrentUser user) {
        return requireStore(user);
    }

    @Transactional
    public StoreEntity updateStore(CurrentUser user, String name, String status) {
        if (name == null || name.isBlank() || name.length() > 128) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "门店名称不能为空且不能超过128个字符");
        }
        if (!ENABLED_STATUS.equals(status) && !"DISABLED".equals(status)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "门店状态不正确");
        }
        StoreEntity store = requireStore(user);
        store.setName(name.trim());
        store.setStatus(status);
        storeMapper.updateById(store);
        return store;
    }

    public List<AdminUserEntity> listStaff(CurrentUser user) {
        return adminUserMapper.selectList(new LambdaQueryWrapper<AdminUserEntity>()
                .eq(AdminUserEntity::getTenantId, user.tenantId())
                .eq(AdminUserEntity::getStoreId, user.storeId())
                .orderByAsc(AdminUserEntity::getId));
    }

    @Transactional
    public AdminUserEntity createStaff(CurrentUser operator, String mobile, String displayName,
                                       String password, Long roleId) {
        if (mobile == null || !mobile.matches("1\\d{10}") || displayName == null || displayName.isBlank()
                || password == null || password.length() < 8) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "员工手机号、姓名或密码不符合要求");
        }
        Long count = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUserEntity>()
                .eq(AdminUserEntity::getTenantId, operator.tenantId())
                .eq(AdminUserEntity::getMobile, mobile));
        if (count > 0) throw new BusinessException(ErrorCode.VALIDATION_FAILED, "该手机号已存在商户员工账号");
        RoleEntity role = requireRole(operator, roleId);
        AdminUserEntity user = new AdminUserEntity();
        user.setId(System.currentTimeMillis()); user.setTenantId(operator.tenantId()); user.setStoreId(operator.storeId());
        user.setMobile(mobile); user.setDisplayName(displayName.trim()); user.setPasswordHash(passwordService.hash(password));
        user.setStatus(ENABLED_STATUS); user.setTokenVersion(1); adminUserMapper.insert(user);
        UserRoleEntity relation = new UserRoleEntity(); relation.setTenantId(operator.tenantId()); relation.setDomain("TENANT");
        relation.setUserId(user.getId()); relation.setRoleId(role.getId()); userRoleMapper.insert(relation);
        auditService.record(operator, AuditEvent.of(AuditAction.MERCHANT_STAFF_CREATED, AuditTarget.of("商户员工", user.getId(), user.getDisplayName())));
        return user;
    }

    @Transactional
    public AdminUserEntity updateStaff(CurrentUser operator, Long staffId, String mobile, String displayName, Long roleId) {
        AdminUserEntity user = requireStaff(operator, staffId);
        if (mobile == null || !mobile.matches("1\\d{10}") || displayName == null || displayName.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "员工手机号或姓名不符合要求");
        }
        requireRole(operator, roleId);
        user.setMobile(mobile); user.setDisplayName(displayName.trim()); adminUserMapper.updateById(user);
        userRoleMapper.delete(new LambdaQueryWrapper<UserRoleEntity>().eq(UserRoleEntity::getTenantId, operator.tenantId())
                .eq(UserRoleEntity::getDomain, "TENANT").eq(UserRoleEntity::getUserId, staffId));
        UserRoleEntity relation = new UserRoleEntity(); relation.setTenantId(operator.tenantId()); relation.setDomain("TENANT");
        relation.setUserId(staffId); relation.setRoleId(roleId); userRoleMapper.insert(relation);
        auditService.record(operator, AuditEvent.of(AuditAction.MERCHANT_STAFF_UPDATED, AuditTarget.of("商户员工", staffId, user.getDisplayName())));
        return user;
    }

    @Transactional
    public void updateStaffStatus(CurrentUser operator, Long staffId, String status) {
        AdminUserEntity user = requireStaff(operator, staffId);
        if (!ENABLED_STATUS.equals(status) && !"DISABLED".equals(status)) throw new BusinessException(ErrorCode.VALIDATION_FAILED, "员工状态不正确");
        if (staffId.equals(operator.userId()) && "DISABLED".equals(status)) throw new BusinessException(ErrorCode.VALIDATION_FAILED, "不能停用当前登录账号");
        user.setStatus(status); user.setTokenVersion((user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1); adminUserMapper.updateById(user);
        AuditAction action = ENABLED_STATUS.equals(status) ? AuditAction.MERCHANT_STAFF_ENABLED : AuditAction.MERCHANT_STAFF_DISABLED;
        auditService.record(operator, AuditEvent.of(action, AuditTarget.of("商户员工", staffId, user.getDisplayName())));
    }

    public List<RoleEntity> listRoles(CurrentUser user) {
        return roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>()
                .eq(RoleEntity::getDomain, "TENANT")
                .eq(RoleEntity::getTenantId, user.tenantId())
                .orderByAsc(RoleEntity::getId));
    }

    @Transactional
    public RoleEntity createRole(CurrentUser operator, String roleCode, String name, String description) {
        if (roleCode == null || !roleCode.matches("merchant_[a-z0-9_]+") || name == null || name.isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商户角色标识或名称不符合要求");
        }
        RoleEntity role = new RoleEntity(); role.setTenantId(operator.tenantId()); role.setDomain("TENANT"); role.setRoleCode(roleCode);
        role.setName(name.trim()); role.setDescription(description); role.setStatus(ENABLED_STATUS); role.setSystemRole(false); role.setAuthorityRole(false); roleMapper.insert(role);
        auditService.record(operator, AuditEvent.of(AuditAction.MERCHANT_ROLE_CREATED, AuditTarget.of("商户角色", role.getId(), role.getName())));
        return role;
    }

    @Transactional
    public RoleEntity updateRole(CurrentUser operator, Long roleId, String name, String description) {
        RoleEntity role = requireRole(operator, roleId);
        if (Boolean.TRUE.equals(role.getSystemRole())) throw new BusinessException(ErrorCode.FORBIDDEN, "系统预置商户角色不可修改");
        if (name == null || name.isBlank()) throw new BusinessException(ErrorCode.VALIDATION_FAILED, "角色名称不能为空");
        role.setName(name.trim()); role.setDescription(description); roleMapper.updateById(role);
        auditService.record(operator, AuditEvent.of(AuditAction.MERCHANT_ROLE_UPDATED, AuditTarget.of("商户角色", roleId, role.getName()))); return role;
    }

    @Transactional
    public void updateRoleStatus(CurrentUser operator, Long roleId, String status) {
        RoleEntity role = requireRole(operator, roleId);
        if (Boolean.TRUE.equals(role.getSystemRole())) throw new BusinessException(ErrorCode.FORBIDDEN, "系统预置商户角色不可停用");
        role.setStatus(status); roleMapper.updateById(role);
        AuditAction action = ENABLED_STATUS.equals(status) ? AuditAction.MERCHANT_ROLE_ENABLED : AuditAction.MERCHANT_ROLE_DISABLED;
        auditService.record(operator, AuditEvent.of(action, AuditTarget.of("商户角色", roleId, role.getName())));
    }

    @Transactional
    public void replaceRolePermissions(CurrentUser operator, Long roleId, List<Long> permissionIds) {
        RoleEntity role = requireRole(operator, roleId);
        if (Boolean.TRUE.equals(role.getSystemRole())) throw new BusinessException(ErrorCode.FORBIDDEN, "系统预置商户角色不可重新配置权限");
        List<PermissionEntity> permissions = permissionIds == null || permissionIds.isEmpty() ? List.of() : permissionMapper.selectList(new LambdaQueryWrapper<PermissionEntity>()
                .eq(PermissionEntity::getDomain, "TENANT").in(PermissionEntity::getId, permissionIds));
        if (permissions.size() != (permissionIds == null ? 0 : permissionIds.stream().distinct().count())) throw new BusinessException(ErrorCode.FORBIDDEN, "只能配置商户权限域的权限点");
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermissionEntity>().eq(RolePermissionEntity::getTenantId, operator.tenantId())
                .eq(RolePermissionEntity::getDomain, "TENANT").eq(RolePermissionEntity::getRoleId, roleId));
        for (PermissionEntity permission : permissions) { RolePermissionEntity relation = new RolePermissionEntity(); relation.setTenantId(operator.tenantId()); relation.setDomain("TENANT"); relation.setRoleId(roleId); relation.setPermissionId(permission.getId()); rolePermissionMapper.insert(relation); }
        auditService.record(operator, AuditEvent.of(AuditAction.MERCHANT_ROLE_PERMISSIONS_UPDATED, AuditTarget.of("商户角色", roleId, role.getName())).withItemCount(permissions.size()));
    }

    public List<Long> listRolePermissionIds(CurrentUser user, Long roleId) {
        requireRole(user, roleId);
        return rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermissionEntity>().eq(RolePermissionEntity::getTenantId, user.tenantId())
                .eq(RolePermissionEntity::getDomain, "TENANT").eq(RolePermissionEntity::getRoleId, roleId)).stream().map(RolePermissionEntity::getPermissionId).toList();
    }

    public MerchantSystemConfigEntity getSystemConfig(CurrentUser user) {
        MerchantSystemConfigEntity config = merchantSystemConfigMapper.selectOne(new LambdaQueryWrapper<MerchantSystemConfigEntity>()
                .eq(MerchantSystemConfigEntity::getTenantId, user.tenantId()).eq(MerchantSystemConfigEntity::getStoreId, user.storeId()));
        if (config == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户展示设置不存在"); return config;
    }

    @Transactional
    public MerchantSystemConfigEntity updateSystemConfig(CurrentUser operator, String shortName, String notice) {
        if (shortName == null || shortName.isBlank() || shortName.length() > 64 || notice == null || notice.length() > 255) throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商户展示设置不符合要求");
        MerchantSystemConfigEntity config = getSystemConfig(operator); config.setShortName(shortName.trim()); config.setNotice(notice.trim()); merchantSystemConfigMapper.updateById(config);
        auditService.record(operator, AuditEvent.of(AuditAction.MERCHANT_SYSTEM_CONFIG_UPDATED, AuditTarget.of("商户展示设置", config.getId(), config.getShortName()))); return config;
    }

    public List<PermissionEntity> listPermissions() {
        return permissionMapper.selectList(new LambdaQueryWrapper<PermissionEntity>()
                .eq(PermissionEntity::getDomain, "TENANT")
                .orderByAsc(PermissionEntity::getId));
    }

    public List<OperationLogEntity> listLogs(CurrentUser user, String keyword, String operationType, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 100);
        LambdaQueryWrapper<OperationLogEntity> query = new LambdaQueryWrapper<OperationLogEntity>()
                .eq(OperationLogEntity::getDomain, "TENANT")
                .eq(OperationLogEntity::getTenantId, user.tenantId())
                .eq(OperationLogEntity::getStoreId, user.storeId())
                .eq(operationType != null && !operationType.isBlank(), OperationLogEntity::getOperationType, operationType)
                .orderByDesc(OperationLogEntity::getCreatedAt)
                .last("limit " + safePageSize + " offset " + ((safePage - 1) * safePageSize));
        if (keyword != null && !keyword.isBlank()) {
            query.and(wrapper -> wrapper.like(OperationLogEntity::getOperationType, keyword)
                    .or().like(OperationLogEntity::getTargetType, keyword)
                    .or().like(OperationLogEntity::getTargetId, keyword)
                    .or().like(OperationLogEntity::getDetail, keyword)
                    .or().like(OperationLogEntity::getTraceId, keyword));
        }
        return operationLogMapper.selectList(query);
    }

    private StoreEntity requireStore(CurrentUser user) {
        StoreEntity store = storeMapper.selectOne(new LambdaQueryWrapper<StoreEntity>()
                .eq(StoreEntity::getId, user.storeId())
                .eq(StoreEntity::getTenantId, user.tenantId()));
        if (store == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "当前商户门店不存在");
        }
        return store;
    }

    private AdminUserEntity requireStaff(CurrentUser user, Long staffId) {
        AdminUserEntity staff = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUserEntity>().eq(AdminUserEntity::getId, staffId)
                .eq(AdminUserEntity::getTenantId, user.tenantId()).eq(AdminUserEntity::getStoreId, user.storeId()));
        if (staff == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户员工不存在"); return staff;
    }

    private RoleEntity requireRole(CurrentUser user, Long roleId) {
        RoleEntity role = roleMapper.selectOne(new LambdaQueryWrapper<RoleEntity>().eq(RoleEntity::getId, roleId)
                .eq(RoleEntity::getDomain, "TENANT").eq(RoleEntity::getTenantId, user.tenantId()));
        if (role == null) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户角色不存在"); return role;
    }
}
