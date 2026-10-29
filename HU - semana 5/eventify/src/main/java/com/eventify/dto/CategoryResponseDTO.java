package com.eventify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de una categoría en la API REST")
public class CategoryResponseDTO {

    @Schema(description = "Identificador persistente de la categoría", example = "1")
    private Long id;

    @Schema(description = "Nombre oficial de la categoría", example = "Concerts")
    private String name;

    @Schema(description = "Descripción temática de la categoría", example = "Conciertos y presentaciones musicales en vivo")
    private String description;

    public String getNombre() {
        return name;
    }

    public void setNombre(String nombre) {
        this.name = nombre;
    }
}
