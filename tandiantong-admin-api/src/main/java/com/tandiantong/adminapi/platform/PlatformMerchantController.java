package com.tandiantong.adminapi.platform;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.tandiantong.security.tenant.MerchantOnboardingCommand;
import com.tandiantong.security.tenant.MerchantProvisioningService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/** 平台商户开通与启用接口。 */
@RestController
@RequestMapping("/api/platform/v1/merchants")
@Tag(name = "平台商户")
public class PlatformMerchantController {

    private final MerchantProvisioningService merchantProvisioningService;

    public PlatformMerchantController(MerchantProvisioningService merchantProvisioningService) {
        this.merchantProvisioningService = merchantProvisioningService;
    }

    @Operation(summary = "开通商户")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantProvisioningResponse create(@Valid @RequestBody CreateMerchantRequest request) {
        MerchantProvisioningService.ProvisionedMerchant merchant = merchantProvisioningService.provision(
                new MerchantOnboardingCommand(request.merchantName(), request.storeAddress(), request.adminName(), request.adminMobile()));
        return new MerchantProvisioningResponse(merchant.tenantId(), merchant.storeId(), merchant.merchantName(),
                merchant.storeName(), merchant.invitationCode(), merchant.invitationExpiresAt().toString(), merchant.sceneKey(),
                merchant.paymentConfigStatus().name());
    }

    @Operation(summary = "查询商户列表")
    @GetMapping
    public List<MerchantProvisioningService.MerchantOverview> list() {
        return merchantProvisioningService.listMerchants();
    }

    @Operation(summary = "启用商户")
    @PostMapping("/{tenantId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable("tenantId") Long tenantId) {
        merchantProvisioningService.enableMerchant(tenantId);
    }

    /** 开通商户请求。 */
    public record CreateMerchantRequest(
            @NotBlank(message = "商户名称不能为空") String merchantName,
            @NotBlank(message = "门店地址不能为空") String storeAddress,
            @NotBlank(message = "管理员姓名不能为空") String adminName,
            @Pattern(regexp = "1\\d{10}", message = "管理员手机号格式不正确") String adminMobile
    ) {
    }

    /** 商户开通响应。 */
    public record MerchantProvisioningResponse(Long tenantId, Long storeId, String merchantName, String storeName,
                                               String invitationCode, String invitationExpiresAt, String sceneKey,
                                               String paymentConfigStatus) {
    }
}
