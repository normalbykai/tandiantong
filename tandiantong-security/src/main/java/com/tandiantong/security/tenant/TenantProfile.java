package com.tandiantong.security.tenant;

/** 租户基础资料。 */
public record TenantProfile(
        Long id,
        String name,
        TenantStatus status
) {
}
