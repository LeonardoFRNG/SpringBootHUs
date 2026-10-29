package com.eventify.dto;

import com.eventify.dto.validation.OnUpdate;
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
 * DTO de entrada específico para la actualización completa de un evento existente.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos requeridos para actualizar un evento existente")
public class EventUpdateDTO {

    @Schema(description = "Identificador único del evento a actualizar", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del evento es obligatorio para la actualización", groups = OnUpdate.class)
    @Min(value = 1, message = "El ID del evento debe ser mayor a 0", groups = OnUpdate.class)
    private Long id;

    @Schema(description = "Nombre actualizado del evento", example = "DevFest Colombia 2026 - Edición Renovada", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre del evento es obligatorio", groups = {OnUpdate.class, Default.class})
    @Size(min = 3, max = 100, message = "El nombre debe contener entre 3 y 100 caracteres", groups = {OnUpdate.class, Default.class})
    private String nombre;

    @Schema(description = "Nueva fecha del evento (no puede ser anterior a hoy)", example = "2026-11-25", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La fecha del evento es obligatoria", groups = {OnUpdate.class, Default.class})
    @NoPastEvents(message = "La fecha del evento no puede ser anterior a hoy", groups = {OnUpdate.class, Default.class})
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @Schema(description = "Descripción actualizada", example = "Agenda ampliada con talleres y sesiones técnicas avanzadas.")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres", groups = {OnUpdate.class, Default.class})
    private String descripcion;

    @Schema(description = "Identificador de la sede asignada", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El identificador de la sede (venueId) es obligatorio", groups = {OnUpdate.class, Default.class})
    @Min(value = 1, message = "El identificador de la sede debe ser mayor a 0", groups = {OnUpdate.class, Default.class})
    private Long venueId;

    @Schema(description = "Conjunto de identificadores de las categorías", example = "[1, 2]")
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
