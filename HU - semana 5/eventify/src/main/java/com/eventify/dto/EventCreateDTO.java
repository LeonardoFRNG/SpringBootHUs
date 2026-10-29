package com.eventify.dto;

import com.eventify.dto.validation.OnCreate;
import com.eventify.validation.NoPastEvents;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * DTO de entrada exclusivo para la creación de eventos.
 * No expone campos de auditoría ni permite enviar el identificador generado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para crear un nuevo evento")
public class EventCreateDTO {

    @Schema(description = "Nombre oficial del evento", example = "DevFest Colombia 2026", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre del evento es obligatorio", groups = {OnCreate.class, Default.class})
    @Size(min = 3, max = 100, message = "El nombre debe contener entre 3 y 100 caracteres", groups = {OnCreate.class, Default.class})
    private String nombre;

    @Schema(description = "Fecha de realización del evento (no puede ser anterior a hoy)", example = "2026-11-20", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha del evento es obligatoria", groups = {OnCreate.class, Default.class})
    @NoPastEvents(message = "La fecha del evento no puede ser anterior a hoy", groups = {OnCreate.class, Default.class})
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @Schema(description = "Descripción temática y detalles del evento", example = "Conferencias sobre microservicios, cloud y arquitectura de software.")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres", groups = {OnCreate.class, Default.class})
    private String descripcion;

    @Schema(description = "Identificador persistente de la sede (Venue) donde se realizará el evento", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El identificador de la sede (venueId) es obligatorio", groups = {OnCreate.class, Default.class})
    @Min(value = 1, message = "El identificador de la sede debe ser mayor a 0", groups = {OnCreate.class, Default.class})
    private Long venueId;

    @Schema(description = "Conjunto de identificadores de las categorías asociadas al evento", example = "[1, 3]")
    @Builder.Default
    private Set<Long> categoryIds = new HashSet<>();

    @com.fasterxml.jackson.annotation.JsonProperty("venue")
    public void setVenueFromObject(java.util.Map<String, Object> venue) {
        if (venue != null && venue.containsKey("id") && venue.get("id") != null) {
            this.venueId = Long.valueOf(venue.get("id").toString());
        }
    }

    @com.fasterxml.jackson.annotation.JsonProperty("categories")
    public void setCategoriesFromObject(java.util.Collection<?> cats) {
        if (cats != null) {
            if (this.categoryIds == null) {
                this.categoryIds = new HashSet<>();
            }
            for (Object c : cats) {
                if (c instanceof java.util.Map<?, ?> map && map.containsKey("id") && map.get("id") != null) {
                    this.categoryIds.add(Long.valueOf(map.get("id").toString()));
                } else if (c instanceof Number num) {
                    this.categoryIds.add(num.longValue());
                }
            }
        }
    }

    // Alias para conveniencia en inglés
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
}
