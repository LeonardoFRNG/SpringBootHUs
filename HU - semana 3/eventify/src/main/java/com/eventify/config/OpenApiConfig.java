package com.eventify.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    /**
     * Limita la documentación de Swagger OpenAPI exclusivamente a las rutas de la API (/api/**),
     * manteniendo las rutas de la interfaz visual web (/admin/**) separadas y limpias.
     */
    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder()
                .group("eventify-api")
                .pathsToMatch("/api/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Eventify REST API")
                        .version("1.0.0")
                        .description("Documentación oficial de los endpoints REST de Eventify. Las rutas administrativas (/admin) son gestionadas por interfaz web.")
                        .contact(new Contact().name("Eventify Team")));
    }
}
