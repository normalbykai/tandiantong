package com.tandiantong.security.context;

import java.util.Optional;

/** 服务端认证后建立的可信当前用户上下文。 */
public record CurrentUser(
        Long userId,
        AccessDomain domain,
        Long tenantId,
        Long storeId,
        String mobile,
        String displayName
) {

    public static CurrentUser platform(Long userId, String mobile, String displayName) {
        return new CurrentUser(userId, AccessDomain.PLATFORM, null, null, mobile, displayName);
    }

    public static CurrentUser tenant(Long userId, Long tenantId, Long storeId, String mobile, String displayName) {
        if (tenantId == null || storeId == null) {
            throw new IllegalArgumentException("租户用户必须包含租户和门店上下文");
        }
        return new CurrentUser(userId, AccessDomain.TENANT, tenantId, storeId, mobile, displayName);
    }

    public Optional<Long> tenantIdOptional() {
        return Optional.ofNullable(tenantId);
    }

    public Optional<Long> storeIdOptional() {
        return Optional.ofNullable(storeId);
    }
}
