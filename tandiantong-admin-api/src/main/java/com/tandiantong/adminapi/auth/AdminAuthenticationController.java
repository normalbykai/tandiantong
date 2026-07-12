package com.tandiantong.adminapi.auth;

import com.tandiantong.security.auth.DatabaseAuthenticationService;
import com.tandiantong.security.tenant.MerchantProvisioningService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminAuthenticationController {

    private final DatabaseAuthenticationService databaseAuthenticationService;
    private final MerchantProvisioningService merchantProvisioningService;

    public AdminAuthenticationController(DatabaseAuthenticationService databaseAuthenticationService,
                                         MerchantProvisioningService merchantProvisioningService) {
        this.databaseAuthenticationService = databaseAuthenticationService;
        this.merchantProvisioningService = merchantProvisioningService;
    }

    @PostMapping("/api/platform/v1/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse platformLogin(@Valid @RequestBody LoginRequest request) {
        return responseOf(databaseAuthenticationService.loginPlatform(request.mobile(), request.password()));
    }

    @PostMapping("/api/admin/v1/auth/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse tenantLogin(@Valid @RequestBody LoginRequest request) {
        return responseOf(databaseAuthenticationService.loginTenant(request.mobile(), request.password()));
    }

    @PostMapping("/api/admin/v1/auth/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activate(@Valid @RequestBody ActivateInvitationRequest request) {
        merchantProvisioningService.activateInvitation(request.invitationCode(), request.password());
    }

    private LoginResponse responseOf(DatabaseAuthenticationService.LoginResult result) {
        return new LoginResponse(result.accessToken(), result.currentUser().domain().name(), result.currentUser().displayName());
    }

    public record LoginRequest(
            @Pattern(regexp = "1\\d{10}", message = "手机号格式不正确") String mobile,
            @NotBlank(message = "密码不能为空") String password
    ) {
    }

    public record LoginResponse(String accessToken, String domain, String displayName) {
    }

    public record ActivateInvitationRequest(
            @NotBlank(message = "邀请码不能为空") String invitationCode,
            @NotBlank(message = "密码不能为空") String password
    ) {
    }
}
