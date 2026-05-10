package com.williamfds.integrationhub.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI integrationHubOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Integration Hub Service")
                .version("0.1")
                .description("Normaliza webhooks de e-commerce em um modelo canônico interno."));
    }
}
