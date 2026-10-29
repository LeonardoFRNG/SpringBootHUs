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
                        .title("Eventify REST API - HU Semana 4")
                        .version("2.0.0")
                        .description("""
                                Documentación oficial de los endpoints REST de Eventify.
                                Novedades Semana 4:
                                - Modelo relacional evolucionado: Event -> Venue (@ManyToOne) y Event <-> Category (@ManyToMany).
                                - Catálogo optimizado con proyecciones Java Record `EventSummaryDTO`.
                                - Paginación eficiente mediante `Slice` (sin COUNT(*) total innecesario).
                                - Mitigación estricta del problema N+1 mediante proyecciones directas y carga de colecciones por lotes.
                                - Borrado lógico (Soft Delete) con Hibernate `@SQLRestriction(\"active = true\")`.
                                - Motor de búsqueda flexible combinable (ciudad, categoría, capacidad, fechas y nombre).
                                - Migraciones versionadas gestionadas por Flyway (V1, V2, V3 con más de 200 eventos iniciales).
                                """)
                        .contact(new Contact().name("Eventify Backend Architecture Team")));
    }
}
