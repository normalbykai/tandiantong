package com.tandiantong.security.context;

import com.tandiantong.framework.security.core.LoginUser;
import com.tandiantong.framework.security.core.context.LoginUserContextHolder;
import com.tandiantong.framework.tenant.core.context.TenantContext;
import com.tandiantong.framework.tenant.core.context.TenantContextHolder;

/** 当前线程可信用户上下文持有器。 */
public final class SecurityContextHolder {

    private static final ThreadLocal<CurrentUser> CURRENT_USER = new ThreadLocal<>();

    private SecurityContextHolder() {
    }

    public static void set(CurrentUser currentUser) {
        CURRENT_USER.set(currentUser);
        LoginUserContextHolder.set(toLoginUser(currentUser));
        TenantContextHolder.set(TenantContext.of(currentUser.tenantId(), currentUser.storeId()));
    }

    public static CurrentUser currentUser() {
        CurrentUser currentUser = CURRENT_USER.get();
        if (currentUser == null) {
            throw new IllegalStateException("当前请求缺少用户上下文");
        }
        return currentUser;
    }

    public static Long currentTenantId() {
        return TenantContextHolder.currentTenantId();
    }

    public static Long currentStoreId() {
        return TenantContextHolder.currentStoreId();
    }

    public static void clear() {
        CURRENT_USER.remove();
        LoginUserContextHolder.clear();
        TenantContextHolder.clear();
    }

    private static LoginUser toLoginUser(CurrentUser currentUser) {
        return new LoginUser(
                currentUser.userId(),
                com.tandiantong.framework.security.core.AccessDomain.valueOf(currentUser.domain().name()),
                currentUser.tenantId(),
                currentUser.storeId(),
                currentUser.mobile(),
                currentUser.displayName());
    }
}
