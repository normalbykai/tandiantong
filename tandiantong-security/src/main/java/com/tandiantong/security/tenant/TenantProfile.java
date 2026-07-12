package com.tandiantong.security.tenant;

public record TenantProfile(
        Long id,
        String name,
        TenantStatus status
) {
}
