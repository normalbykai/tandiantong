package com.tandiantong.security.tenant;

/** 门店基础资料。 */
public record StoreProfile(
        Long id,
        Long tenantId,
        String name,
        String address
) {
}
