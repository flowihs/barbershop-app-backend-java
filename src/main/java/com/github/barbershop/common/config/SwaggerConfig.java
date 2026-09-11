package com.github.barbershop.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI barbershopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Barbershop Telegram Mini App API")
                        .description("REST API для управления барбершопом через Telegram Mini App")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Barbershop Team")
                                .email("team@barbershop.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT-токен, полученный из POST /api/auth/telegram")));
    }
}
