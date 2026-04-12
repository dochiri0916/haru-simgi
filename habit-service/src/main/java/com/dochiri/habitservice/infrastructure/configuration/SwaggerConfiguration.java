package com.dochiri.habitservice.infrastructure.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Habit Service API")
                        .description("습관 CRUD, 잔디 집계 (완료 0건→0, 1건→1, 2건→2, 3~4건→3, 5건+→4)")
                        .version("v1.0.0")
                );
    }

}
