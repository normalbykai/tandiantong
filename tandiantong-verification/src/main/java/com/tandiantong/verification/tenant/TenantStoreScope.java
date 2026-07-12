package com.tandiantong.verification.tenant;

public record TenantStoreScope(Long tenantId, Long storeId, Long operatorUserId) {

    public TenantStoreScope {
        if (tenantId == null || storeId == null || operatorUserId == null) {
            throw new IllegalArgumentException("核销操作必须包含租户、门店和操作人");
        }
    }
}
