package com.tandiantong.security.auth;

import com.tandiantong.framework.common.api.ErrorCode;
import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.security.context.AccessDomain;

/**
 * Sa-Token 登录标识，统一编码权限域和用户主键。
 */
public record SaTokenLoginId(AccessDomain domain, Long userId) {

    private static final String SEPARATOR = ":";

    /**
     * 创建 Sa-Token 登录标识文本。
     */
    public String encode() {
        return domain.name() + SEPARATOR + userId;
    }

    /**
     * 解析 Sa-Token 登录标识文本。
     */
    public static SaTokenLoginId parse(Object loginId) {
        if (!(loginId instanceof String value)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录状态无效或已过期");
        }
        String[] parts = value.split(SEPARATOR, 2);
        if (parts.length != 2) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录状态无效或已过期");
        }
        try {
            return new SaTokenLoginId(AccessDomain.valueOf(parts[0]), Long.valueOf(parts[1]));
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "登录状态无效或已过期");
        }
    }
}
