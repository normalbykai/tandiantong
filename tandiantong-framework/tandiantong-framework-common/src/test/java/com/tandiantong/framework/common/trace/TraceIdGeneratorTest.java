package com.tandiantong.framework.common.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TraceIdGeneratorTest {

    @Test
    void shouldCreateReadableTraceIdWhenHeaderIsBlank() {
        String traceId = TraceIdGenerator.resolve(null);

        assertThat(traceId).startsWith("tdt-");
        assertThat(traceId).hasSizeGreaterThan(20);
    }

    @Test
    void shouldReuseSafeIncomingTraceId() {
        String traceId = TraceIdGenerator.resolve("client-trace-123");

        assertThat(traceId).isEqualTo("client-trace-123");
    }

    @Test
    void shouldRegenerateTraceIdWhenIncomingValueIsTooLong() {
        String traceId = TraceIdGenerator.resolve("x".repeat(80));

        assertThat(traceId).startsWith("tdt-");
    }
}
