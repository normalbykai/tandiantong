package com.tandiantong.common.trace;

/** 请求追踪号生成工具。 */
public final class TraceIdGenerator {

    private TraceIdGenerator() {
    }

    public static String resolve(String incomingTraceId) {
        return com.tandiantong.framework.common.trace.TraceIdGenerator.resolve(incomingTraceId);
    }
}
