package com.tandiantong.security.tenant;

/** 平台开通商户结果。 */
public record MerchantOnboardingResult(
        TenantProfile tenant,
        StoreProfile store,
        AdminInvitation adminInvitation,
        MiniProgramScene miniProgramScene,
        PaymentConfigStatus paymentConfigStatus
) {
}
