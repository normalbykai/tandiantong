package com.tandiantong.adminapi.platform;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.tandiantong.adminapi.platform.dto.CreateMerchantRequest;
import com.tandiantong.adminapi.platform.dto.MerchantProvisioningResponse;
import com.tandiantong.adminapi.platform.dto.MerchantOverviewResponse;
import com.tandiantong.security.tenant.MerchantOnboardingCommand;
import com.tandiantong.security.tenant.MerchantProvisioningService;
import com.tandiantong.security.context.SecurityContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 平台商户开通与启用接口。 */
@RestController
@RequestMapping("/api/platform/v1/merchants")
@Tag(name = "平台商户", description = "平台管理员开通、查询和启用商户")
public class PlatformMerchantController {

    private final MerchantProvisioningService merchantProvisioningService;

    public PlatformMerchantController(MerchantProvisioningService merchantProvisioningService) {
        this.merchantProvisioningService = merchantProvisioningService;
    }

    @Operation(summary = "开通商户", description = "创建租户、默认门店、管理员邀请和小程序入口码")
    @PostMapping
    @SaCheckPermission("platform:merchant:create")
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantProvisioningResponse create(@Valid @RequestBody CreateMerchantRequest request) {
        MerchantProvisioningService.ProvisionedMerchant merchant = merchantProvisioningService.provision(
                new MerchantOnboardingCommand(request.merchantName(), request.storeAddress(), request.adminName(), request.adminMobile()));
        return new MerchantProvisioningResponse(merchant.tenantId(), merchant.storeId(), merchant.merchantName(),
                merchant.storeName(), merchant.invitationCode(), merchant.invitationExpiresAt().toString(), merchant.sceneKey(),
                merchant.paymentConfigStatus().name());
    }

    @Operation(summary = "查询商户列表", description = "查询商户基础信息、管理员状态和支付配置状态")
    @GetMapping
    @SaCheckPermission("platform:merchant:read")
    public List<MerchantOverviewResponse> list(
            @Parameter(description = "商户名称、管理员姓名或手机号关键字", example = "春风")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "商户状态筛选，例如 ENABLED 或 DISABLED", example = "ENABLED")
            @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "管理员账号状态筛选，例如 PENDING_ACTIVATION 或 ACTIVATED", example = "ACTIVATED")
            @RequestParam(value = "adminStatus", required = false) String adminStatus) {
        return merchantProvisioningService.listMerchants(keyword, status, adminStatus).stream().map(MerchantOverviewResponse::from).toList();
    }

    @Operation(summary = "启用商户", description = "启用已完成管理员激活的商户，使其可以进行业务写操作")
    @PostMapping("/{tenantId}/enable")
    @SaCheckPermission("platform:merchant:enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(
            @Parameter(description = "租户 ID", example = "1001", required = true)
            @PathVariable("tenantId") Long tenantId) {
        merchantProvisioningService.enableMerchant(tenantId);
    }

    @Operation(summary = "停用商户", description = "停用后商户后台登录和后续业务写操作都会被阻止，历史数据不删除")
    @PostMapping("/{tenantId}/disable")
    @SaCheckPermission("platform:merchant:disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(
            @Parameter(description = "租户 ID", example = "1001", required = true)
            @PathVariable("tenantId") Long tenantId) {
        merchantProvisioningService.disableMerchant(SecurityContextHolder.currentUser(), tenantId);
    }

    @Operation(summary = "重新生成管理员邀请码", description = "仅允许管理员尚未激活的商户使用，生成后所有旧邀请码立即失效")
    @PostMapping("/{tenantId}/invitation/reissue")
    @SaCheckPermission("platform:merchant:invitation:reissue")
    public ReissuedInvitationResponse reissueInvitation(
            @Parameter(description = "租户 ID", example = "1001", required = true)
            @PathVariable("tenantId") Long tenantId) {
        MerchantProvisioningService.ReissuedInvitation invitation = merchantProvisioningService.reissueInvitation(
                SecurityContextHolder.currentUser(), tenantId);
        return new ReissuedInvitationResponse(invitation.getInvitationCode(), invitation.getInvitationExpiresAt().toString());
    }

    /** 重新生成邀请码响应，明文邀请码仅在本次接口响应中返回。 */
    @Schema(description = "重新生成商户管理员邀请码响应")
    public static class ReissuedInvitationResponse {
        @Schema(description = "新的商户管理员邀请码", example = "invite-0fc8a0c90e3440b6aeeb1e31e279d0a5")
        private final String invitationCode;
        @Schema(description = "邀请码过期时间", example = "2026-07-25T10:00:00+08:00")
        private final String invitationExpiresAt;

        public ReissuedInvitationResponse(String invitationCode, String invitationExpiresAt) {
            this.invitationCode = invitationCode;
            this.invitationExpiresAt = invitationExpiresAt;
        }

        public String getInvitationCode() {
            return invitationCode;
        }

        public String getInvitationExpiresAt() {
            return invitationExpiresAt;
        }
    }
}
