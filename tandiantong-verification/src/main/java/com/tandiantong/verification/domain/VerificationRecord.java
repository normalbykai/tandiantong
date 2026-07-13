package com.tandiantong.verification.domain;

import java.time.Instant;

/** 已完成核销的审计记录。 */
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
