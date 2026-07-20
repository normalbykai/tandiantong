package com.tandiantong.common.trace;

/** 当前线程请求追踪号上下文。 */
public final class TraceIdContext {

    private TraceIdContext() {
    }

    /** 写入当前请求追踪号。 */
    public static void set(String traceId) {
        com.tandiantong.framework.common.trace.TraceIdContext.set(traceId);
    }

    /** 获取当前请求追踪号。 */
    public static String get() {
        return com.tandiantong.framework.common.trace.TraceIdContext.get();
    }

    /** 清理当前请求追踪号，避免线程复用时串号。 */
    public static void clear() {
        com.tandiantong.framework.common.trace.TraceIdContext.clear();
    }
}
