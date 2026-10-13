package com.example.sample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI sampleServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Sample Service API")
                .version("v1")
                .description("Sample service built from the template"));
    }
}
