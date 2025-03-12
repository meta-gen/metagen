package com.koboolean.metagen.security.conf;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.models.GroupedOpenApi;

@OpenAPIDefinition(
        info = @Info(title = "Spring Boot Swagger Application - Meta-Gen",
                description = "Spring Boot로 개발하는 Meta-Gen RESTful API 명세서 입니다.",
                version = "v1"))
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi chatOpenApi() {
        String[] paths = {"/api/**"};

        return GroupedOpenApi.builder()
                .group("Spring Boot Swagger Application")
                .pathsToMatch(paths)
                .build();
    }
}
