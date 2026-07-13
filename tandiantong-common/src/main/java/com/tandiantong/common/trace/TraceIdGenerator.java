package com.tandiantong.common.trace;

import java.security.SecureRandom;
import java.time.Instant;

/** 请求追踪号生成工具。 */
public final class TraceIdGenerator {

    private static final int MAX_INCOMING_TRACE_ID_LENGTH = 64;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TraceIdGenerator() {
    }

    public static String resolve(String incomingTraceId) {
        if (incomingTraceId != null && !incomingTraceId.isBlank()
                && incomingTraceId.length() <= MAX_INCOMING_TRACE_ID_LENGTH) {
            return incomingTraceId.trim();
        }
        return "tdt-" + Instant.now().toEpochMilli() + "-" + Long.toUnsignedString(RANDOM.nextLong(), 36);
    }
}
