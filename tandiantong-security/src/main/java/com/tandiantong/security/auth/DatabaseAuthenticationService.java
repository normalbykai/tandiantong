package com.tandiantong.security.auth;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.security.context.AccessDomain;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.entity.AdminUserEntity;
import com.tandiantong.security.entity.PlatformUserEntity;
import com.tandiantong.security.entity.TenantEntity;
import com.tandiantong.security.mapper.AdminUserMapper;
import com.tandiantong.security.mapper.PlatformUserMapper;
import com.tandiantong.security.mapper.TenantMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 数据库认证服务，负责登录校验和令牌用户解析。
 */
@Service
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseAuthenticationService {

    private static final String ENABLED_STATUS = "ENABLED";
    private static final String TOKEN_VERSION_KEY = "tokenVersion";

    private final PlatformUserMapper platformUserMapper;
    private final AdminUserMapper adminUserMapper;
    private final TenantMapper tenantMapper;
    private final PasswordService passwordService = new PasswordService();

    public DatabaseAuthenticationService(PlatformUserMapper platformUserMapper, AdminUserMapper adminUserMapper,
                                         TenantMapper tenantMapper) {
        this.platformUserMapper = platformUserMapper;
        this.adminUserMapper = adminUserMapper;
        this.tenantMapper = tenantMapper;
    }

    /**
     * 平台管理员登录。
     */
    public LoginResult loginPlatform(String mobile, String password) {
        PlatformUserEntity user = requireActivePlatformUser(platformUserMapper.selectOne(
                new LambdaQueryWrapper<PlatformUserEntity>().eq(PlatformUserEntity::getMobile, mobile)));
        verifyPassword(password, user.getPasswordHash());
        CurrentUser currentUser = CurrentUser.platform(user.getId(), user.getMobile(), user.getDisplayName());
        return new LoginResult(issueSaToken(user.getId(), AccessDomain.PLATFORM, user.getTokenVersion()), currentUser);
    }

    /**
     * 租户后台用户登录。
     */
    public LoginResult loginTenant(String mobile, String password) {
        AdminUserEntity user = requireActiveTenantUser(adminUserMapper.selectOne(
                new LambdaQueryWrapper<AdminUserEntity>().eq(AdminUserEntity::getMobile, mobile)));
        verifyPassword(password, user.getPasswordHash());
        ensureTenantEnabled(user.getTenantId());
        CurrentUser currentUser = CurrentUser.tenant(user.getId(), user.getTenantId(), user.getStoreId(),
                user.getMobile(), user.getDisplayName());
        return new LoginResult(issueSaToken(user.getId(), AccessDomain.TENANT, user.getTokenVersion()), currentUser);
    }

    /**
     * 根据当前 Sa-Token 登录态解析用户上下文。
     */
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
        return CurrentUser.tenant(user.getId(), user.getTenantId(), user.getStoreId(), user.getMobile(), user.getDisplayName());
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
        if (tokenVersionInSession == null || !tokenVersionInSession.equals(tokenVersionInDatabase)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录状态已失效");
        }
    }

    private void ensureTenantEnabled(Long tenantId) {
        TenantEntity tenant = tenantMapper.selectById(tenantId);
        if (tenant == null || !ENABLED_STATUS.equals(tenant.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前商户暂未启用或已停用");
        }
    }

    private String issueSaToken(Long userId, AccessDomain domain, Integer tokenVersion) {
        StpUtil.login(new SaTokenLoginId(domain, userId).encode());
        StpUtil.getTokenSession().set(TOKEN_VERSION_KEY, tokenVersion);
        return StpUtil.getTokenValue();
    }

    public record LoginResult(String accessToken, CurrentUser currentUser) {
    }
}
