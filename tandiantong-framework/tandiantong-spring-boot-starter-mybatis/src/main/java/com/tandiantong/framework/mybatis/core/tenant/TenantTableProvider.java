package com.tandiantong.framework.mybatis.core.tenant;

import java.util.Set;

/** 提供需要自动追加 tenant_id 条件的业务表集合。 */
public interface TenantTableProvider {

    Set<String> tenantTables();
}
