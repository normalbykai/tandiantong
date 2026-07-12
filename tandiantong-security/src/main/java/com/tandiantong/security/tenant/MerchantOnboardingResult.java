package com.tandiantong.security.tenant;

public record MerchantOnboardingResult(
        TenantProfile tenant,
        StoreProfile store,
        AdminInvitation adminInvitation,
        MiniProgramScene miniProgramScene,
        PaymentConfigStatus paymentConfigStatus
) {
}
