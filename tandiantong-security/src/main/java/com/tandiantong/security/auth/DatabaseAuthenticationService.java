package com.tandiantong.security.auth;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.security.context.AccessDomain;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.entity.AdminUserEntity;
import com.tandiantong.security.entity.PlatformUserEntity;
import com.tandiantong.security.entity.RoleEntity;
import com.tandiantong.security.entity.TenantEntity;
import com.tandiantong.security.entity.UserRoleEntity;
import com.tandiantong.security.mapper.AdminUserMapper;
import com.tandiantong.security.mapper.PlatformUserMapper;
import com.tandiantong.security.mapper.RoleMapper;
import com.tandiantong.security.mapper.TenantMapper;
import com.tandiantong.security.mapper.UserRoleMapper;
import com.tandiantong.security.rbac.PermissionAuthorizationService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

/** 数据库认证服务，负责登录校验和令牌用户解析。 */
@Service
@ConditionalOnProperty(
        prefix = "tandiantong.security",
        name = "database-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class DatabaseAuthenticationService {

    private static final String ENABLED_STATUS = "ENABLED";
    private static final String TOKEN_VERSION_KEY = "tokenVersion";
    private static final long REMEMBER_ME_TIMEOUT_SECONDS = 7 * 24 * 60 * 60L;

    private final PlatformUserMapper platformUserMapper;
    private final AdminUserMapper adminUserMapper;
    private final TenantMapper tenantMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionAuthorizationService permissionAuthorizationService;
    private final PasswordService passwordService = new PasswordService();

    public DatabaseAuthenticationService(
            PlatformUserMapper platformUserMapper,
            AdminUserMapper adminUserMapper,
            TenantMapper tenantMapper,
            RoleMapper roleMapper,
            UserRoleMapper userRoleMapper,
            PermissionAuthorizationService permissionAuthorizationService) {
        this.platformUserMapper = platformUserMapper;
        this.adminUserMapper = adminUserMapper;
        this.tenantMapper = tenantMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionAuthorizationService = permissionAuthorizationService;
    }

    /** 平台管理员登录。 */
    public LoginResult loginPlatform(String mobile, String password, boolean rememberMe) {
        PlatformUserEntity user =
                requireActivePlatformUser(
                        platformUserMapper.selectOne(
                                new LambdaQueryWrapper<PlatformUserEntity>()
                                        .eq(PlatformUserEntity::getMobile, mobile)));
        verifyPassword(password, user.getPasswordHash());
        CurrentUser currentUser =
                CurrentUser.platform(user.getId(), user.getMobile(), user.getDisplayName());
        List<String> roleNames = resolveRoleNames(AccessDomain.PLATFORM, user.getId(), null);
        List<String> permissionCodes =
                permissionAuthorizationService.listPermissionCodes(
                        AccessDomain.PLATFORM, null, user.getId());
        return new LoginResult(
                issueSaToken(
                        user.getId(), AccessDomain.PLATFORM, user.getTokenVersion(), rememberMe),
                currentUser,
                firstRoleName(roleNames, AccessDomain.PLATFORM),
                roleNames,
                permissionCodes);
    }

    /** 租户后台用户登录。 */
    public LoginResult loginTenant(String mobile, String password, boolean rememberMe) {
        AdminUserEntity user =
                requireActiveTenantUser(
                        adminUserMapper.selectOne(
                                new LambdaQueryWrapper<AdminUserEntity>()
                                        .eq(AdminUserEntity::getMobile, mobile)));
        verifyPassword(password, user.getPasswordHash());
        ensureTenantEnabled(user.getTenantId());
        CurrentUser currentUser =
                CurrentUser.tenant(
                        user.getId(),
                        user.getTenantId(),
                        user.getStoreId(),
                        user.getMobile(),
                        user.getDisplayName());
        List<String> roleNames =
                resolveRoleNames(AccessDomain.TENANT, user.getId(), user.getTenantId());
        List<String> permissionCodes =
                permissionAuthorizationService.listPermissionCodes(
                        AccessDomain.TENANT, user.getTenantId(), user.getId());
        return new LoginResult(
                issueSaToken(user.getId(), AccessDomain.TENANT, user.getTokenVersion(), rememberMe),
                currentUser,
                firstRoleName(roleNames, AccessDomain.TENANT),
                roleNames,
                permissionCodes);
    }

    /** 根据当前 Sa-Token 登录态解析用户上下文。 */
    public CurrentUser resolveCurrentSaTokenUser(AccessDomain expectedDomain) {
        StpUtil.checkLogin();
        SaTokenLoginId loginId = SaTokenLoginId.parse(StpUtil.getLoginId());
        if (loginId.domain() != expectedDomain) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前账号没有操作权限");
        }
        Integer tokenVersion = (Integer) StpUtil.getTokenSession().get(TOKEN_VERSION_KEY);
        if (loginId.domain() == AccessDomain.PLATFORM) {
            return resolvePlatform(loginId.userId(), tokenVersion);
        }
        return resolveTenant(loginId.userId(), tokenVersion);
    }

    private CurrentUser resolvePlatform(Long userId, Integer tokenVersion) {
        PlatformUserEntity user = requireActivePlatformUser(platformUserMapper.selectById(userId));
        ensureTokenVersion(tokenVersion, user.getTokenVersion());
        return CurrentUser.platform(user.getId(), user.getMobile(), user.getDisplayName());
    }

    private CurrentUser resolveTenant(Long userId, Integer tokenVersion) {
        AdminUserEntity user = requireActiveTenantUser(adminUserMapper.selectById(userId));
        ensureTokenVersion(tokenVersion, user.getTokenVersion());
        ensureTenantEnabled(user.getTenantId());
        return CurrentUser.tenant(
                user.getId(),
                user.getTenantId(),
                user.getStoreId(),
                user.getMobile(),
                user.getDisplayName());
    }

    private PlatformUserEntity requireActivePlatformUser(PlatformUserEntity user) {
        if (user == null || !ENABLED_STATUS.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码不正确");
        }
        return user;
    }

    private AdminUserEntity requireActiveTenantUser(AdminUserEntity user) {
        if (user == null || !ENABLED_STATUS.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码不正确");
        }
        return user;
    }

    private void verifyPassword(String password, String passwordHash) {
        if (password == null || !passwordService.matches(password, passwordHash)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码不正确");
        }
    }

    private void ensureTokenVersion(Integer tokenVersionInSession, Integer tokenVersionInDatabase) {
        if (tokenVersionInSession == null
                || !tokenVersionInSession.equals(tokenVersionInDatabase)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录状态已失效");
        }
    }

    private void ensureTenantEnabled(Long tenantId) {
        TenantEntity tenant = tenantMapper.selectById(tenantId);
        if (tenant == null || !ENABLED_STATUS.equals(tenant.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前商户暂未启用或已停用");
        }
    }

    private String issueSaToken(
            Long userId, AccessDomain domain, Integer tokenVersion, boolean rememberMe) {
        if (rememberMe) {
            SaLoginModel loginModel =
                    new SaLoginModel()
                            .setTimeout(REMEMBER_ME_TIMEOUT_SECONDS)
                            .setActiveTimeout(REMEMBER_ME_TIMEOUT_SECONDS);
            StpUtil.login(new SaTokenLoginId(domain, userId).encode(), loginModel);
        } else {
            StpUtil.login(new SaTokenLoginId(domain, userId).encode());
        }
        StpUtil.getTokenSession().set(TOKEN_VERSION_KEY, tokenVersion);
        return StpUtil.getTokenValue();
    }

    /** 登录时返回全部有效角色名称，供管理端记录当前账号权限身份。 */
    private List<String> resolveRoleNames(AccessDomain domain, Long userId, Long tenantId) {
        String domainName = domain.name();
        List<UserRoleEntity> relations =
                userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRoleEntity>()
                                .eq(UserRoleEntity::getDomain, domainName)
                                .eq(UserRoleEntity::getUserId, userId)
                                .eq(tenantId != null, UserRoleEntity::getTenantId, tenantId)
                                .isNull(tenantId == null, UserRoleEntity::getTenantId)
                                .orderByAsc(UserRoleEntity::getId));
        return relations.stream()
                .map(UserRoleEntity::getRoleId)
                .map(roleMapper::selectById)
                .filter(role -> role != null && ENABLED_STATUS.equals(role.getStatus()))
                .map(RoleEntity::getName)
                .toList();
    }

    private String firstRoleName(List<String> roleNames, AccessDomain domain) {
        return roleNames.stream()
                .findFirst()
                .orElse(domain == AccessDomain.PLATFORM ? "平台账号" : "商户账号");
    }

    public static class LoginResult {
        private final String accessToken;
        private final CurrentUser currentUser;
        private final String roleName;
        private final List<String> roleNames;
        private final List<String> permissionCodes;

        public LoginResult(
                String accessToken,
                CurrentUser currentUser,
                String roleName,
                List<String> roleNames,
                List<String> permissionCodes) {
            this.accessToken = accessToken;
            this.currentUser = currentUser;
            this.roleName = roleName;
            this.roleNames = List.copyOf(roleNames);
            this.permissionCodes = List.copyOf(permissionCodes);
        }

        public String accessToken() {
            return accessToken;
        }

        public CurrentUser currentUser() {
            return currentUser;
        }

        public String roleName() {
            return roleName;
        }

        public List<String> roleNames() {
            return roleNames;
        }

        public List<String> permissionCodes() {
            return permissionCodes;
        }
    }
}
