package com.jirama.interfaces.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI jiramaOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("JIRAMA CRM API")
                        .description("REST API for JIRAMA CRM Web Platform — " +
                                "Manage subscribers, contracts, meters, billing, " +
                                "payments, incidents, and customer support.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("JIRAMA IT Department")
                                .email("it@jirama.mg")
                                .url("https://jirama.mg"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://jirama.mg")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api")
                                .description("Local Development"),
                        new Server().url("https://api.jirama.mg/api")
                                .description("Production")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Keycloak JWT token. " +
                                                "Get it from Keycloak: " +
                                                "/realms/jirama/protocol/openid-connect/token")));
    }
}
