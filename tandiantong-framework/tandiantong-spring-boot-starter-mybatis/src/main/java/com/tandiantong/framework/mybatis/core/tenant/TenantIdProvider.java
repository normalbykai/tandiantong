package com.tandiantong.framework.mybatis.core.tenant;

/** 为 MyBatis 租户拦截器提供可信租户编号。 */
public interface TenantIdProvider {

    Long currentTenantId();
}
