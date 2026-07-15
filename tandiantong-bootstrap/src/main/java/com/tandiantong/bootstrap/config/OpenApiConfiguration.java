package com.tandiantong.bootstrap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Knife4j 接口文档全局配置。 */
@Configuration
public class OpenApiConfiguration {

    private static final String TOKEN_SCHEME_NAME = "Authorization";

    @Bean
    public OpenAPI tandiantongOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("摊点通接口文档")
                        .description("摊点通 V1.1 后台、小程序和平台接口")
                        .version("1.1.0"))
                .components(new Components().addSecuritySchemes(TOKEN_SCHEME_NAME,
                        new SecurityScheme()
                                .name(TOKEN_SCHEME_NAME)
                                .description("请输入登录响应中的 accessToken，接口调试时将按 Bearer 认证方式携带")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("Sa-Token")));
    }

    /**
     * 为受保护接口显式声明认证要求。
     *
     * <p>Knife4j 4.5 调试页面不会将 OpenAPI 根级安全声明继承到具体接口，因此必须写入每个受保护操作，
     * 才会把授权页填写的令牌附加到调试请求。</p>
     */
    @Bean
    public OpenApiCustomizer protectedOperationSecurityCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            openApi.getPaths().forEach((path, pathItem) -> {
                if (!isProtectedPath(path)) {
                    return;
                }
                pathItem.readOperations().forEach(operation ->
                        operation.addSecurityItem(new SecurityRequirement().addList(TOKEN_SCHEME_NAME)));
            });
        };
    }

    private boolean isProtectedPath(String path) {
        return isProtectedPlatformPath(path) || isProtectedTenantPath(path);
    }

    private boolean isProtectedPlatformPath(String path) {
        return path.startsWith("/api/platform/v1/")
                && !path.equals("/api/platform/v1/health")
                && !path.equals("/api/platform/v1/auth/login");
    }

    private boolean isProtectedTenantPath(String path) {
        return path.startsWith("/api/admin/v1/")
                && !path.equals("/api/admin/v1/auth/login")
                && !path.equals("/api/admin/v1/auth/activate");
    }
}
