package com.tandiantong.bootstrap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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
                                .description("请输入 Bearer token，例如：Bearer 9f3f1c2a-1111-2222-3333-abcdef123456")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("Sa-Token")))
                .addSecurityItem(new SecurityRequirement().addList(TOKEN_SCHEME_NAME));
    }
}
