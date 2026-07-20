package com.tandiantong.framework.ip.core;

import com.tandiantong.framework.ip.core.util.IpAddressUtils;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/** 从 HTTP 请求中解析客户端真实 IP。 */
public class ClientIpResolver {

    private static final List<String> CLIENT_IP_HEADERS = List.of(
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    );

    public String resolve(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        for (String header : CLIENT_IP_HEADERS) {
            String ip = firstValidIp(request.getHeader(header));
            if (ip != null) {
                return ip;
            }
        }
        return IpAddressUtils.normalize(request.getRemoteAddr());
    }

    private String firstValidIp(String headerValue) {
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        String[] candidates = headerValue.split(",");
        for (String candidate : candidates) {
            String ip = IpAddressUtils.normalize(candidate);
            if (ip != null && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return null;
    }
}
