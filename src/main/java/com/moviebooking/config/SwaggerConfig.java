package com.moviebooking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("MooviBooki Movie Booking API")
                        .version("v1")
                        .description(
                                "Comprehensive, production-ready movie booking API with JWT security, seat locking, caching, and more."))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .schemaRequirement(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"))
                .addTagsItem(new Tag().name("Auth").description("User authentication and registration"))
                .addTagsItem(new Tag().name("Movies").description("Movie catalog and management"))
                .addTagsItem(new Tag().name("Theaters").description("Theater and screen management"))
                .addTagsItem(new Tag().name("Bookings").description("Booking and seat selection"))
                .addTagsItem(new Tag().name("Admin").description("Admin dashboard and analytics"));
    }
}