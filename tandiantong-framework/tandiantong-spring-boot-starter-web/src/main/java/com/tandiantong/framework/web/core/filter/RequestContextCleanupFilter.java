package com.tandiantong.framework.web.core.filter;

import com.tandiantong.framework.common.context.ThreadLocalContextCleaner;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.filter.OncePerRequestFilter;

/** 请求结束后统一清理框架层线程上下文。 */
public class RequestContextCleanupFilter extends OncePerRequestFilter {

    private final ObjectProvider<ThreadLocalContextCleaner> cleaners;

    public RequestContextCleanupFilter(ObjectProvider<ThreadLocalContextCleaner> cleaners) {
        this.cleaners = cleaners;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            cleaners.orderedStream().forEach(ThreadLocalContextCleaner::clear);
        }
    }
}
