package com.tandiantong.framework.tenant.core.util;

import com.tandiantong.framework.tenant.core.context.TenantContext;
import com.tandiantong.framework.tenant.core.context.TenantContextHolder;

/** 租户上下文工具。 */
public final class TenantUtils {

    private TenantUtils() {
    }

    public static void runWithTenant(Long tenantId, Long storeId, Runnable runnable) {
        TenantContext previous = null;
        boolean hasPrevious = true;
        try {
            previous = TenantContextHolder.current();
        } catch (IllegalStateException exception) {
            hasPrevious = false;
        }
        TenantContextHolder.set(TenantContext.of(tenantId, storeId));
        try {
            runnable.run();
        } finally {
            if (hasPrevious) {
                TenantContextHolder.set(previous);
            } else {
                TenantContextHolder.clear();
            }
        }
    }
}
