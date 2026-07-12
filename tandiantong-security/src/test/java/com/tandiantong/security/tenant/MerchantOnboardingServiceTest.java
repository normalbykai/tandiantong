package com.tandiantong.security.tenant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MerchantOnboardingServiceTest {

    @Test
    void shouldCreateTenantStoreAdminInvitationAndSceneKey() {
        MerchantOnboardingService service = new MerchantOnboardingService();
        MerchantOnboardingCommand command = new MerchantOnboardingCommand(
                "春风小铺",
                "上海路 18 号",
                "张晓春",
                "13800008000"
        );

        MerchantOnboardingResult result = service.createMerchant(command);

        assertThat(result.tenant().name()).isEqualTo("春风小铺");
        assertThat(result.tenant().status()).isEqualTo(TenantStatus.PENDING_ENABLE);
        assertThat(result.store().tenantId()).isEqualTo(result.tenant().id());
        assertThat(result.store().name()).isEqualTo("春风小铺默认门店");
        assertThat(result.adminInvitation().mobile()).isEqualTo("13800008000");
        assertThat(result.adminInvitation().invitationCode()).hasSizeGreaterThan(20);
        assertThat(result.miniProgramScene().sceneKey()).hasSizeGreaterThan(20);
        assertThat(result.miniProgramScene().sceneKey()).doesNotContain(String.valueOf(result.tenant().id()));
    }

    @Test
    void paidBusinessShouldBeBlockedWhenPaymentIsNotVerified() {
        MerchantOnboardingService service = new MerchantOnboardingService();

        assertThat(service.canPublishPaidBusiness(PaymentConfigStatus.NOT_CONFIGURED)).isFalse();
        assertThat(service.canPublishPaidBusiness(PaymentConfigStatus.PENDING_VERIFY)).isFalse();
        assertThat(service.canPublishPaidBusiness(PaymentConfigStatus.VERIFIED)).isTrue();
    }
}
