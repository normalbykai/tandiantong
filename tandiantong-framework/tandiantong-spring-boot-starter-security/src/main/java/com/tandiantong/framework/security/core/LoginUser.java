package com.tandiantong.framework.security.core;

import java.util.Optional;

/** 认证后建立的通用登录用户视图。 */
public class LoginUser {

    private final Long userId;

    private final AccessDomain domain;

    private final Long tenantId;

    private final Long storeId;

    private final String mobile;

    private final String displayName;

    public LoginUser(Long userId, AccessDomain domain, Long tenantId, Long storeId, String mobile, String displayName) {
        this.userId = userId;
        this.domain = domain;
        this.tenantId = tenantId;
        this.storeId = storeId;
        this.mobile = mobile;
        this.displayName = displayName;
    }

    public Long userId() {
        return userId;
    }

    public AccessDomain domain() {
        return domain;
    }

    public Long tenantId() {
        return tenantId;
    }

    public Long storeId() {
        return storeId;
    }

    public String mobile() {
        return mobile;
    }

    public String displayName() {
        return displayName;
    }

    public Optional<Long> tenantIdOptional() {
        return Optional.ofNullable(tenantId);
    }

    public Optional<Long> storeIdOptional() {
        return Optional.ofNullable(storeId);
    }
}
