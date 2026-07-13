package com.tandiantong.security.tenant;

/** 小程序入口场景配置。 */
public record MiniProgramScene(
        Long tenantId,
        String sceneKey,
        boolean enabled
) {
}
