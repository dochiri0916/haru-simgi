package com.dochiri.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SwaggerAutoConfiguration {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    OpenApiCustomizer jwtSecuritySchemeCustomizer() {
        return openApi -> openApi
                .components(
                        (openApi.getComponents() != null ? openApi.getComponents() : new Components())
                                .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT Access Token (Authorization 헤더 또는 쿠키로 전달)")
                                )
                )
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
