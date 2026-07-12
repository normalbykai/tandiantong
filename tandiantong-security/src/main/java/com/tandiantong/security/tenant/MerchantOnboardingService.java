package com.tandiantong.security.tenant;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;

public class MerchantOnboardingService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AtomicLong idSequence = new AtomicLong(1000);

    public MerchantOnboardingResult createMerchant(MerchantOnboardingCommand command) {
        Long tenantId = idSequence.incrementAndGet();
        TenantProfile tenant = new TenantProfile(tenantId, command.merchantName(), TenantStatus.PENDING_ENABLE);
        StoreProfile store = new StoreProfile(idSequence.incrementAndGet(), tenantId,
                command.merchantName() + "默认门店", command.storeAddress());
        AdminInvitation invitation = new AdminInvitation(tenantId, command.adminName(), command.adminMobile(),
                secureRandomKey("invite"), Instant.now().plus(7, ChronoUnit.DAYS));
        MiniProgramScene scene = new MiniProgramScene(tenantId, secureRandomKey("scene"), true);
        return new MerchantOnboardingResult(tenant, store, invitation, scene, PaymentConfigStatus.NOT_CONFIGURED);
    }

    public boolean canPublishPaidBusiness(PaymentConfigStatus paymentConfigStatus) {
        return paymentConfigStatus == PaymentConfigStatus.VERIFIED;
    }

    private String secureRandomKey(String prefix) {
        return prefix + "-" + Long.toUnsignedString(RANDOM.nextLong(), 36)
                + Long.toUnsignedString(RANDOM.nextLong(), 36);
    }
}
