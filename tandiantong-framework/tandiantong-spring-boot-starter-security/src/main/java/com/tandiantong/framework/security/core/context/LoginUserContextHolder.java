package com.tandiantong.framework.security.core.context;

import com.tandiantong.framework.security.core.LoginUser;

/** 当前线程登录用户上下文持有器。 */
public final class LoginUserContextHolder {

    private static final ThreadLocal<LoginUser> CURRENT = new ThreadLocal<>();

    private LoginUserContextHolder() {
    }

    public static void set(LoginUser loginUser) {
        CURRENT.set(loginUser);
    }

    public static LoginUser currentUser() {
        LoginUser loginUser = CURRENT.get();
        if (loginUser == null) {
            throw new IllegalStateException("当前请求缺少用户上下文");
        }
        return loginUser;
    }

    public static void clear() {
        CURRENT.remove();
    }
}
