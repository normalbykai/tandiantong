package com.tandiantong.analytics.tenant;

/** 统计模块可信租户门店操作范围。 */
public record TenantStoreScope(Long tenantId, Long storeId, Long operatorUserId) {
}
