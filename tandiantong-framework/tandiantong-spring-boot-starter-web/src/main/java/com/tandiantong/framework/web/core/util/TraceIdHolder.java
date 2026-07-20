package com.tandiantong.framework.web.core.util;

import com.tandiantong.framework.common.trace.TraceIdContext;

/** 当前线程请求追踪号持有器。 */
public final class TraceIdHolder {

    private TraceIdHolder() {
    }

    public static void set(String traceId) {
        TraceIdContext.set(traceId);
    }

    public static String get() {
        return TraceIdContext.get();
    }

    public static void clear() {
        TraceIdContext.clear();
    }
}
