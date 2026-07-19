package com.tandiantong.adminapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** 后台登录响应。 */
@Schema(description = "后台登录响应")
public class LoginResponse {

    @Schema(description = "Sa-Token 访问令牌，调用受保护接口时放入 Authorization 请求头", example = "9f3f1c2a-1111-2222-3333-abcdef123456")
    private String accessToken;

    @Schema(description = "登录身份权限域，平台为 PLATFORM，商户为 TENANT", example = "TENANT")
    private String domain;

    @Schema(description = "当前登录用户展示名称", example = "张店长")
    private String displayName;

    @Schema(description = "当前登录用户的首个有效角色名称", example = "系统管理员")
    private String roleName;

    @Schema(description = "当前登录用户的全部有效角色名称", example = "[\"系统管理员\", \"账号管理员\"]")
    private List<String> roleNames;

    @Schema(description = "当前登录用户拥有的业务权限编码，按钮展示仅用于改善体验，后端接口仍会独立鉴权", example = "[\"platform:merchant:read\", \"platform:merchant:create\"]")
    private List<String> permissionCodes;

    public LoginResponse(String accessToken, String domain, String displayName, String roleName, List<String> roleNames, List<String> permissionCodes) {
        this.accessToken = accessToken;
        this.domain = domain;
        this.displayName = displayName;
        this.roleName = roleName;
        this.roleNames = List.copyOf(roleNames);
        this.permissionCodes = List.copyOf(permissionCodes);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getDomain() {
        return domain;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRoleName() {
        return roleName;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }
}
