package com.tandiantong.bootstrap.security;

import com.tandiantong.security.auth.DatabaseAuthenticationService;
import com.tandiantong.security.context.AccessDomain;
import com.tandiantong.security.context.CurrentUser;
import com.tandiantong.security.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Sa-Token 请求认证拦截器，负责建立业务用户上下文。
 */
@Component
public class SaTokenAuthenticationInterceptor implements HandlerInterceptor {

    private final DatabaseAuthenticationService databaseAuthenticationService;

    public SaTokenAuthenticationInterceptor(DatabaseAuthenticationService databaseAuthenticationService) {
        this.databaseAuthenticationService = databaseAuthenticationService;
    }

    /**
     * 根据接口分区校验登录身份，并把可信租户上下文写入线程变量。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            return true;
        }
        if (path.startsWith("/api/platform/v1/")) {
            setCurrentUser(AccessDomain.PLATFORM);
            return true;
        }
        if (path.startsWith("/api/admin/v1/")) {
            setCurrentUser(AccessDomain.TENANT);
            return true;
        }
        return true;
    }

    /**
     * 请求结束后清理线程上下文，避免线程复用导致串租户。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        SecurityContextHolder.clear();
    }

    private void setCurrentUser(AccessDomain expectedDomain) {
        CurrentUser currentUser = databaseAuthenticationService.resolveCurrentSaTokenUser(expectedDomain);
        SecurityContextHolder.set(currentUser);
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/platform/v1/health")
                || path.equals("/actuator/health")
                || path.startsWith("/foundation-test/")
                || path.startsWith("/api/callback/")
                || path.equals("/api/platform/v1/auth/login")
                || path.equals("/api/admin/v1/auth/login")
                || path.equals("/api/admin/v1/auth/activate")
                || path.equals("/api/mini/v1/catalog/products")
                || path.equals("/api/mini/v1/orders")
                || path.equals("/api/mini/v1/reservations")
                || path.equals("/api/mini/v1/reservations/services")
                || path.equals("/doc.html")
                || path.startsWith("/webjars/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs");
    }
}
