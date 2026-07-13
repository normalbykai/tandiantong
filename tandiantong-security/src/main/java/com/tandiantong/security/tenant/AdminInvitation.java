package com.tandiantong.security.tenant;

import java.time.Instant;

/** 商户管理员邀请信息。 */
public record AdminInvitation(
        Long tenantId,
        String adminName,
        String mobile,
        String invitationCode,
        Instant expiresAt
) {
}
