package com.tandiantong.framework.operatelog.core.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tandiantong.framework.ip.core.ClientIpResolver;
import com.tandiantong.framework.operatelog.core.model.OperationLogCommand;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mock.web.MockHttpServletRequest;

class OperationLogRequestEnricherTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldEnrichCommandFromCurrentRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin/v1/orders/refund");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
        request.addHeader("User-Agent", "Mozilla/5.0");
        ObjectProvider<HttpServletRequest> requestProvider = mock(ObjectProvider.class);
        when(requestProvider.getIfAvailable()).thenReturn(request);
        OperationLogRequestEnricher enricher = new OperationLogRequestEnricher(
                new ClientIpResolver(),
                requestProvider);
        OperationLogCommand command = new OperationLogCommand();

        enricher.enrich(command);

        assertThat(command.getUserIp()).isEqualTo("203.0.113.10");
        assertThat(command.getUserAgent()).isEqualTo("Mozilla/5.0");
        assertThat(command.getRequestMethod()).isEqualTo("POST");
        assertThat(command.getRequestUrl()).isEqualTo("/api/admin/v1/orders/refund");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldIgnoreMissingRequest() {
        ObjectProvider<HttpServletRequest> requestProvider = mock(ObjectProvider.class);
        when(requestProvider.getIfAvailable()).thenReturn(null);
        OperationLogRequestEnricher enricher = new OperationLogRequestEnricher(
                new ClientIpResolver(),
                requestProvider);
        OperationLogCommand command = new OperationLogCommand();

        enricher.enrich(command);

        assertThat(command.getUserIp()).isNull();
        assertThat(command.getRequestUrl()).isNull();
    }
}
