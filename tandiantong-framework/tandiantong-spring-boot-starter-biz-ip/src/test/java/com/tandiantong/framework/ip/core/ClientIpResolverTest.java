package com.tandiantong.framework.ip.core;

import static org.assertj.core.api.Assertions.assertThat;

import com.tandiantong.framework.ip.core.util.IpAddressUtils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ClientIpResolverTest {

    private final ClientIpResolver resolver = new ClientIpResolver();

    @Test
    void shouldUseFirstForwardedIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
        request.setRemoteAddr("127.0.0.1");

        assertThat(resolver.resolve(request)).isEqualTo("203.0.113.10");
    }

    @Test
    void shouldFallbackToRemoteAddress() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "unknown");
        request.setRemoteAddr("192.168.1.10");

        assertThat(resolver.resolve(request)).isEqualTo("192.168.1.10");
    }

    @Test
    void shouldIdentifyInternalAddress() {
        assertThat(IpAddressUtils.isInternal("127.0.0.1")).isTrue();
        assertThat(IpAddressUtils.isInternal("192.168.1.10")).isTrue();
        assertThat(IpAddressUtils.isInternal("203.0.113.10")).isFalse();
    }
}
