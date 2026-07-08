package com.srm.creditengine.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI srmCreditEngineOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SRM Credit Engine API")
                .description("Backend scaffold for the SRM Credit Engine challenge.")
                .version("v1")
                .contact(new Contact().name("SRM Credit Engine")));
    }
}
