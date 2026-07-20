package com.tandiantong.framework.operatelog.core.service;

import com.tandiantong.framework.ip.core.ClientIpResolver;
import com.tandiantong.framework.operatelog.core.model.OperationLogCommand;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;

/** 从当前 HTTP 请求补全操作日志上下文。 */
public class OperationLogRequestEnricher {

    private static final int MAX_USER_IP_LENGTH = 64;

    private static final int MAX_USER_AGENT_LENGTH = 512;

    private static final int MAX_REQUEST_METHOD_LENGTH = 16;

    private static final int MAX_REQUEST_URL_LENGTH = 512;

    private final ClientIpResolver clientIpResolver;

    private final ObjectProvider<HttpServletRequest> requestProvider;

    public OperationLogRequestEnricher(ClientIpResolver clientIpResolver, ObjectProvider<HttpServletRequest> requestProvider) {
        this.clientIpResolver = clientIpResolver;
        this.requestProvider = requestProvider;
    }

    public void enrich(OperationLogCommand command) {
        if (command == null) {
            return;
        }
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request == null) {
            return;
        }
        command.setUserIp(limit(clientIpResolver.resolve(request), MAX_USER_IP_LENGTH));
        command.setUserAgent(limit(request.getHeader("User-Agent"), MAX_USER_AGENT_LENGTH));
        command.setRequestMethod(limit(request.getMethod(), MAX_REQUEST_METHOD_LENGTH));
        command.setRequestUrl(limit(request.getRequestURI(), MAX_REQUEST_URL_LENGTH));
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
