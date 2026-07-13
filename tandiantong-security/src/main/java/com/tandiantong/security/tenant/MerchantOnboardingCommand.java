package com.tandiantong.security.tenant;

/** 平台开通商户命令。 */
public record MerchantOnboardingCommand(
        String merchantName,
        String storeAddress,
        String adminName,
        String adminMobile
) {
}
