package com.tandiantong.security.tenant;

import java.time.Instant;

public record AdminInvitation(
        Long tenantId,
        String adminName,
        String mobile,
        String invitationCode,
        Instant expiresAt
) {
}
