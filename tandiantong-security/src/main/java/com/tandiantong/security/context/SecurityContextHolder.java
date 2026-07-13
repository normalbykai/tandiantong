package com.tandiantong.security.context;

/** 当前线程可信用户上下文持有器。 */
public final class SecurityContextHolder {

    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private SecurityContextHolder() {
    }

    public static void set(CurrentUser currentUser) {
        CURRENT_USER.set(currentUser);
    }

    public static CurrentUser currentUser() {
        CurrentUser currentUser = CURRENT_USER.get();
        if (currentUser == null) {
            throw new IllegalStateException("当前请求缺少用户上下文");
        }
        return currentUser;
    }

    public static Long currentTenantId() {
        return currentUser().tenantIdOptional()
                .orElseThrow(() -> new IllegalStateException("当前请求缺少租户上下文"));
    }

    public static Long currentStoreId() {
        return currentUser().storeIdOptional()
                .orElseThrow(() -> new IllegalStateException("当前请求缺少门店上下文"));
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
