package com.tandiantong.framework.tenant.core.context;

import java.util.Optional;

/** 当前线程租户与门店上下文。 */
public class TenantContext {

    private final Long tenantId;

    private final Long storeId;

    public TenantContext(Long tenantId, Long storeId) {
        this.tenantId = tenantId;
        this.storeId = storeId;
    }

    public static TenantContext of(Long tenantId, Long storeId) {
        return new TenantContext(tenantId, storeId);
    }

    public Long tenantId() {
        return tenantId;
    }

    public Long storeId() {
        return storeId;
    }

    public Optional<Long> tenantIdOptional() {
        return Optional.ofNullable(tenantId);
    }

    public Optional<Long> storeIdOptional() {
        return Optional.ofNullable(storeId);
    }
}
