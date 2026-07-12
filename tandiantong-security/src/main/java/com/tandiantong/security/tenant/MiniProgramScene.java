package com.tandiantong.security.tenant;

public record MiniProgramScene(
        Long tenantId,
        String sceneKey,
        boolean enabled
) {
}
