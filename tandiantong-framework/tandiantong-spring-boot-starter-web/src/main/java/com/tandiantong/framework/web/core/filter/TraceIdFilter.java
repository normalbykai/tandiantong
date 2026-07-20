package com.tandiantong.framework.web.core.filter;

import com.tandiantong.framework.common.trace.TraceIdGenerator;
import com.tandiantong.framework.web.core.util.TraceIdHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/** 为每个 HTTP 请求建立并回传追踪号。 */
public class TraceIdFilter extends OncePerRequestFilter {

    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = TraceIdGenerator.resolve(request.getHeader(TRACE_ID_HEADER));
        TraceIdHolder.set(traceId);
        MDC.put("traceId", traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
            TraceIdHolder.clear();
        }
    }
}
