package com.tandiantong.security.tenant;

public record StoreProfile(
        Long id,
        Long tenantId,
        String name,
        String address
) {
}
