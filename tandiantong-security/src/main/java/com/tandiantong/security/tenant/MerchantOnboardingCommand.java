package com.tandiantong.security.tenant;

public record MerchantOnboardingCommand(
        String merchantName,
        String storeAddress,
        String adminName,
        String adminMobile
) {
}
