package com.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * DTO optimizado mediante Java Record para listados masivos y proyecciones JPQL constructor.
 * Evita la carga de entidades completas en memoria y previene el problema N+1.
 */
@Schema(description = "Resumen optimizado de evento para listados masivos y búsquedas")
public record EventSummaryDTO(
        @Schema(description = "Identificador único del evento", example = "1")
        Long id,

        @Schema(description = "Nombre del evento", example = "DevFest Colombia 2026")
        String name,

        @Schema(description = "Fecha de realización del evento", example = "2026-11-15")
        LocalDate date,

        @Schema(description = "Descripción resumida del evento", example = "Congreso de arquitectura de software y nube")
        String description,

        @Schema(description = "Nombre de la sede (venue)", example = "Movistar Arena")
        String venueName,

        @Schema(description = "Ciudad donde se ubica la sede", example = "Bogotá")
        String venueCity,

        @Schema(description = "Capacidad máxima de la sede", example = "14000")
        Integer venueCapacity,

        @Schema(description = "Nombres de las categorías asociadas al evento", example = "[\"Conferences\", \"Workshops\"]")
        List<String> categories
) {

    /**
     * Constructor utilizado por la proyección JPQL SELECT NEW.
     * Recupera de forma eficiente los campos base en una única consulta indexada.
     */
    public EventSummaryDTO(Long id, String name, LocalDate date, String description, String venueName, String venueCity, Integer venueCapacity) {
        this(id, name, date, description, venueName, venueCity, venueCapacity, Collections.emptyList());
    }

    /**
     * Devuelve una nueva instancia enriquecida con la lista de categorías cargada por lotes (evitando N+1).
     */
    public EventSummaryDTO withCategories(List<String> newCategories) {
        return new EventSummaryDTO(
                this.id,
                this.name,
                this.date,
                this.description,
                this.venueName,
                this.venueCity,
                this.venueCapacity,
                newCategories != null ? newCategories : Collections.emptyList()
        );
    }

    // Métodos alias para compatibilidad total con plantillas y convenciones en español
    public String getNombre() {
        return name;
    }

    public LocalDate getFecha() {
        return date;
    }

    public String getDescripcion() {
        return description;
    }

    public String getLugar() {
        return venueName;
    }

    public String getCiudad() {
        return venueCity;
    }

    public Integer getCapacidad() {
        return venueCapacity;
    }

    public List<String> getCategorias() {
        return categories;
    }
}
