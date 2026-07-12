package com.tandiantong.security.auth;

import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.security.context.AccessDomain;
import com.tandiantong.security.context.CurrentUser;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class DatabaseAuthenticationService {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordService passwordService = new PasswordService();
    private final TokenService tokenService;

    public DatabaseAuthenticationService(JdbcTemplate jdbcTemplate, TokenService tokenService) {
        this.jdbcTemplate = jdbcTemplate;
        this.tokenService = tokenService;
    }

    public LoginResult loginPlatform(String mobile, String password) {
        List<LoginUser> users = jdbcTemplate.query(
                "select id, mobile, display_name, password_hash, status, token_version from platform_user where mobile = ?",
                (resultSet, rowNumber) -> new LoginUser(resultSet.getLong("id"), resultSet.getString("mobile"),
                        resultSet.getString("display_name"), resultSet.getString("password_hash"), resultSet.getString("status"),
                        resultSet.getInt("token_version"), null, null), mobile);
        LoginUser user = requireActiveUser(users);
        verifyPassword(password, user);
        CurrentUser currentUser = CurrentUser.platform(user.userId(), user.mobile(), user.displayName());
        return new LoginResult(tokenService.issue(user.userId(), AccessDomain.PLATFORM, user.tokenVersion()), currentUser);
    }

    public LoginResult loginTenant(String mobile, String password) {
        List<LoginUser> users = jdbcTemplate.query(
                "select id, mobile, display_name, password_hash, status, token_version, tenant_id, store_id from admin_user where mobile = ?",
                (resultSet, rowNumber) -> new LoginUser(resultSet.getLong("id"), resultSet.getString("mobile"),
                        resultSet.getString("display_name"), resultSet.getString("password_hash"), resultSet.getString("status"),
                        resultSet.getInt("token_version"), resultSet.getLong("tenant_id"), resultSet.getLong("store_id")), mobile);
        LoginUser user = requireActiveUser(users);
        verifyPassword(password, user);
        ensureTenantEnabled(user.tenantId());
        CurrentUser currentUser = CurrentUser.tenant(user.userId(), user.tenantId(), user.storeId(), user.mobile(), user.displayName());
        return new LoginResult(tokenService.issue(user.userId(), AccessDomain.TENANT, user.tokenVersion()), currentUser);
    }

    public CurrentUser resolve(AuthTokenClaims claims) {
        return claims.domain() == AccessDomain.PLATFORM ? resolvePlatform(claims) : resolveTenant(claims);
    }

    private CurrentUser resolvePlatform(AuthTokenClaims claims) {
        List<LoginUser> users = jdbcTemplate.query(
                "select id, mobile, display_name, password_hash, status, token_version from platform_user where id = ?",
                (resultSet, rowNumber) -> new LoginUser(resultSet.getLong("id"), resultSet.getString("mobile"),
                        resultSet.getString("display_name"), resultSet.getString("password_hash"), resultSet.getString("status"),
                        resultSet.getInt("token_version"), null, null), claims.userId());
        LoginUser user = requireActiveUser(users);
        ensureTokenVersion(claims, user);
        return CurrentUser.platform(user.userId(), user.mobile(), user.displayName());
    }

    private CurrentUser resolveTenant(AuthTokenClaims claims) {
        List<LoginUser> users = jdbcTemplate.query(
                "select id, mobile, display_name, password_hash, status, token_version, tenant_id, store_id from admin_user where id = ?",
                (resultSet, rowNumber) -> new LoginUser(resultSet.getLong("id"), resultSet.getString("mobile"),
                        resultSet.getString("display_name"), resultSet.getString("password_hash"), resultSet.getString("status"),
                        resultSet.getInt("token_version"), resultSet.getLong("tenant_id"), resultSet.getLong("store_id")), claims.userId());
        LoginUser user = requireActiveUser(users);
        ensureTokenVersion(claims, user);
        ensureTenantEnabled(user.tenantId());
        return CurrentUser.tenant(user.userId(), user.tenantId(), user.storeId(), user.mobile(), user.displayName());
    }

    private LoginUser requireActiveUser(List<LoginUser> users) {
        if (users.size() != 1 || !"ENABLED".equals(users.getFirst().status())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码不正确");
        }
        return users.getFirst();
    }

    private void verifyPassword(String password, LoginUser user) {
        if (password == null || !passwordService.matches(password, user.passwordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "账号或密码不正确");
        }
    }

    private void ensureTokenVersion(AuthTokenClaims claims, LoginUser user) {
        if (claims.tokenVersion() != user.tokenVersion()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录状态已失效");
        }
    }

    private void ensureTenantEnabled(Long tenantId) {
        String status = jdbcTemplate.queryForObject("select status from tenant where id = ?", String.class, tenantId);
        if (!"ENABLED".equals(status)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "当前商户暂未启用或已停用");
        }
    }

    public record LoginResult(String accessToken, CurrentUser currentUser) {
    }

    private record LoginUser(Long userId, String mobile, String displayName, String passwordHash, String status,
                             int tokenVersion, Long tenantId, Long storeId) {
    }
}
