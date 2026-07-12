package com.tandiantong.verification.domain;

import java.time.LocalDate;

public record VerificationCredential(
        Long credentialId,
        Long tenantId,
        Long storeId,
        BusinessType businessType,
        String businessNo,
        String summary,
        LocalDate businessDate,
        String pickupNo,
        String verificationToken,
        VerificationStatus status
) {

    public VerificationCredential verified() {
        return new VerificationCredential(credentialId, tenantId, storeId, businessType, businessNo, summary,
                businessDate, pickupNo, verificationToken, VerificationStatus.VERIFIED);
    }
}
