package com.tandiantong.security.rbac;

import cn.dev33.satoken.stp.StpInterface;
import com.tandiantong.security.auth.SaTokenLoginId;
import com.tandiantong.security.context.AccessDomain;
import com.tandiantong.security.entity.AdminUserEntity;
import com.tandiantong.security.mapper.AdminUserMapper;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Sa-Token 权限数据提供器，从数据库角色关系加载权限编码。 */
@Component
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class SaTokenPermissionProvider implements StpInterface {

    private final PermissionAuthorizationService permissionAuthorizationService;
    private final AdminUserMapper adminUserMapper;

    public SaTokenPermissionProvider(PermissionAuthorizationService permissionAuthorizationService,
                                     AdminUserMapper adminUserMapper) {
        this.permissionAuthorizationService = permissionAuthorizationService;
        this.adminUserMapper = adminUserMapper;
    }

    /** 为 Sa-Token 提供当前登录身份拥有的权限编码。 */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaTokenLoginId parsedLoginId = SaTokenLoginId.parse(loginId);
        if (parsedLoginId.domain() == AccessDomain.PLATFORM) {
            return permissionAuthorizationService.listPermissionCodes(AccessDomain.PLATFORM, null, parsedLoginId.userId());
        }
        AdminUserEntity user = adminUserMapper.selectById(parsedLoginId.userId());
        if (user == null) {
            return List.of();
        }
        return permissionAuthorizationService.listPermissionCodes(AccessDomain.TENANT, user.getTenantId(), user.getId());
    }

    /** 当前阶段只使用细粒度权限编码校验，角色名不作为接口授权依据。 */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return List.of();
    }
}
