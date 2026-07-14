package com.tandiantong.adminapi.auth;

import com.tandiantong.security.auth.DatabaseAuthenticationService;
import com.tandiantong.security.tenant.MerchantProvisioningService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@Tag(name = "后台认证")
public class AdminAuthenticationController {

    private final DatabaseAuthenticationService databaseAuthenticationService;
    private final MerchantProvisioningService merchantProvisioningService;

    public AdminAuthenticationController(DatabaseAuthenticationService databaseAuthenticationService,
                                         MerchantProvisioningService merchantProvisioningService) {
        this.databaseAuthenticationService = databaseAuthenticationService;
        this.merchantProvisioningService = merchantProvisioningService;
    }

    @Operation(summary = "平台管理员登录")
    @PostMapping("/api/platform/v1/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse platformLogin(@Valid @RequestBody PlatformLoginRequest request) {
        return responseOf(databaseAuthenticationService.loginPlatform(request.mobile(), request.password()));
    }

    @Operation(summary = "商户后台用户登录")
    @PostMapping("/api/admin/v1/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse tenantLogin(@Valid @RequestBody LoginRequest request) {
        return responseOf(databaseAuthenticationService.loginTenant(request.mobile(), request.password()));
    }

    @Operation(summary = "激活商户管理员邀请")
    @PostMapping("/api/admin/v1/auth/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@Valid @RequestBody ActivateInvitationRequest request) {
        merchantProvisioningService.activateInvitation(request.invitationCode(), request.password());
    }

    private LoginResponse responseOf(DatabaseAuthenticationService.LoginResult result) {
        return new LoginResponse(result.accessToken(), result.currentUser().domain().name(), result.currentUser().displayName());
    }

    /** 平台登录请求。 */
    public record PlatformLoginRequest(
            @NotBlank(message = "账号不能为空") String mobile,
            @NotBlank(message = "密码不能为空") String password
    ) {
    }

    /** 商户后台登录请求。 */
    public record LoginRequest(
            @Pattern(regexp = "1\\d{10}", message = "手机号格式不正确") String mobile,
            @NotBlank(message = "密码不能为空") String password
    ) {
    }

    /** 后台登录响应。 */
    public record LoginResponse(String accessToken, String domain, String displayName) {
    }

    /** 商户管理员邀请激活请求。 */
    public record ActivateInvitationRequest(
            @NotBlank(message = "邀请码不能为空") String invitationCode,
            @NotBlank(message = "密码不能为空") String password
    ) {
    }
}
