package com.tandiantong.security.auth;

import com.tandiantong.security.context.AccessDomain;
import java.time.Instant;

public record AuthTokenClaims(
        Long userId,
        AccessDomain domain,
        int tokenVersion,
        Instant expiresAt
) {
}
