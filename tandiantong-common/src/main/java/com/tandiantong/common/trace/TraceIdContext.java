package com.tandiantong.common.trace;

/** 当前线程请求追踪号上下文。 */
public final class TraceIdContext {

    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private TraceIdContext() {
    }

    /** 写入当前请求追踪号。 */
    public static void set(String traceId) {
        TRACE_ID.set(traceId);
    }

    /** 获取当前请求追踪号。 */
    public static String get() {
        return TRACE_ID.get();
    }

    /** 清理当前请求追踪号，避免线程复用时串号。 */
    public static void clear() {
        TRACE_ID.remove();
    }
}
