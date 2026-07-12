package com.tandiantong.verification.domain;

import java.time.Instant;

public record VerificationRecord(
        Long recordId,
        Long tenantId,
        Long storeId,
        BusinessType businessType,
        String businessNo,
        String summary,
        VerificationStatus status,
        Long operatorUserId,
        String reason,
        Instant verifiedAt
) {
}
