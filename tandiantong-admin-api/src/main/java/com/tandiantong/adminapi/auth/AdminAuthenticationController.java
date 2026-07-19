package com.tandiantong.adminapi.auth;

import com.tandiantong.adminapi.auth.dto.ActivateInvitationRequest;
import com.tandiantong.adminapi.auth.dto.LoginRequest;
import com.tandiantong.adminapi.auth.dto.LoginResponse;
import com.tandiantong.adminapi.auth.dto.PlatformLoginRequest;
import com.tandiantong.security.auth.DatabaseAuthenticationService;
import com.tandiantong.security.tenant.MerchantProvisioningService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台认证接口，提供平台登录、商户登录和邀请激活能力。
 */
@RestController
@Tag(name = "后台认证", description = "平台管理员与商户后台用户登录，以及商户管理员邀请激活")
public class AdminAuthenticationController {

    private final DatabaseAuthenticationService databaseAuthenticationService;
    private final MerchantProvisioningService merchantProvisioningService;

    public AdminAuthenticationController(DatabaseAuthenticationService databaseAuthenticationService,
                                         MerchantProvisioningService merchantProvisioningService) {
        this.databaseAuthenticationService = databaseAuthenticationService;
        this.merchantProvisioningService = merchantProvisioningService;
    }

    @Operation(summary = "平台管理员登录", description = "校验平台管理员账号和密码；勾选七天自动登录时签发七天有效令牌")
    @PostMapping("/api/platform/v1/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse platformLogin(@Valid @RequestBody PlatformLoginRequest request) {
        return responseOf(databaseAuthenticationService.loginPlatform(request.getMobile(), request.getPassword(), request.isRememberMe()));
    }

    @Operation(summary = "商户后台用户登录", description = "校验商户后台用户手机号和密码；勾选七天自动登录时签发七天有效令牌")
    @PostMapping("/api/admin/v1/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse tenantLogin(@Valid @RequestBody LoginRequest request) {
        return responseOf(databaseAuthenticationService.loginTenant(request.getMobile(), request.getPassword(), request.isRememberMe()));
    }

    @Operation(summary = "激活商户管理员邀请", description = "使用平台开通商户时生成的邀请码设置管理员首次登录密码")
    @PostMapping("/api/admin/v1/auth/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@Valid @RequestBody ActivateInvitationRequest request) {
        merchantProvisioningService.activateInvitation(request.invitationCode(), request.password());
    }

    private LoginResponse responseOf(DatabaseAuthenticationService.LoginResult result) {
        return new LoginResponse(result.accessToken(), result.currentUser().domain().name(), result.currentUser().displayName(), result.roleName(), result.roleNames(), result.permissionCodes());
    }
}
