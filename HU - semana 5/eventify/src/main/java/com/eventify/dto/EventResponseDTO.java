package com.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de salida para respuestas individuales de eventos en la API REST.
 * Desacopla completamente las entidades JPA de la respuesta al cliente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta detallada de un evento para consultas individuales de la API REST")
public class EventResponseDTO {

    @Schema(description = "Identificador persistente del evento", example = "1")
    private Long id;

    @Schema(description = "Nombre oficial del evento", example = "DevFest Colombia 2026")
    private String nombre;

    @Schema(description = "Fecha programada de realización", example = "2026-11-20")
    private LocalDate fecha;

    @Schema(description = "Descripción temática del evento", example = "Conferencias de microservicios y arquitectura cloud.")
    private String descripcion;

    @Schema(description = "Estado de actividad del evento (borrado lógico)", example = "true")
    private boolean active;

    @Schema(description = "Nombre de la sede asignada", example = "Movistar Arena")
    private String venueName;

    @Schema(description = "Ciudad donde se ubica la sede", example = "Bogotá")
    private String venueCity;

    @Schema(description = "Nombres de las categorías temáticas asociadas", example = "[\"Conferences\", \"Workshops\"]")
    @Builder.Default
    private List<String> categoryNames = new ArrayList<>();

    // Métodos alias para interoperabilidad en inglés y español
    public String getName() {
        return nombre;
    }

    public void setName(String name) {
        this.nombre = name;
    }

    public LocalDate getDate() {
        return fecha;
    }

    public void setDate(LocalDate date) {
        this.fecha = date;
    }

    public String getDescription() {
        return descripcion;
    }

    public void setDescription(String description) {
        this.descripcion = description;
    }

    public List<String> getCategories() {
        return categoryNames;
    }

    public void setCategories(List<String> categories) {
        this.categoryNames = categories;
    }
}
