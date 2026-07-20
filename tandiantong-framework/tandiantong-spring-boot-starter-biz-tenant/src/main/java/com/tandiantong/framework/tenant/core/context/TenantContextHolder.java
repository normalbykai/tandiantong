package com.tandiantong.framework.tenant.core.context;

/** 当前线程可信租户上下文持有器。 */
public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CURRENT = new ThreadLocal<>();

    private TenantContextHolder() {
    }

    public static void set(TenantContext context) {
        CURRENT.set(context);
    }

    public static TenantContext current() {
        TenantContext context = CURRENT.get();
        if (context == null) {
            throw new IllegalStateException("当前请求缺少租户上下文");
        }
        return context;
    }

    public static Long currentTenantId() {
        return current().tenantIdOptional()
                .orElseThrow(() -> new IllegalStateException("当前请求缺少租户编号"));
    }

    public static Long currentStoreId() {
        return current().storeIdOptional()
                .orElseThrow(() -> new IllegalStateException("当前请求缺少门店编号"));
    }

    public static void clear() {
        CURRENT.remove();
    }
}
