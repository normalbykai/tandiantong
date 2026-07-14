package com.tandiantong.adminapi.platform;

import com.tandiantong.adminapi.platform.dto.CreateMerchantRequest;
import com.tandiantong.adminapi.platform.dto.MerchantProvisioningResponse;
import com.tandiantong.security.tenant.MerchantOnboardingCommand;
import com.tandiantong.security.tenant.MerchantProvisioningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
