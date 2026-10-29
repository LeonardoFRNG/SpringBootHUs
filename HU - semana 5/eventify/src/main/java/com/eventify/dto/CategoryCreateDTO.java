package com.eventify.dto;

import com.eventify.dto.validation.OnCreate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para crear una nueva categoría de eventos")
public class CategoryCreateDTO {

    @Schema(description = "Nombre único de la categoría", example = "Concerts", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre de la categoría es obligatorio", groups = {OnCreate.class, Default.class})
    @Size(min = 2, max = 100, message = "El nombre de la categoría debe contener entre 2 y 100 caracteres", groups = {OnCreate.class, Default.class})
    private String name;

    @Schema(description = "Descripción temática de la categoría", example = "Conciertos, recitales y presentaciones de música en vivo.")
    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres", groups = {OnCreate.class, Default.class})
    private String description;

    public String getNombre() {
        return name;
    }

    public void setNombre(String nombre) {
        this.name = nombre;
    }
}
