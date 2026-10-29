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
@Schema(description = "Respuesta de una sede en la API REST")
public class VenueResponseDTO {

    @Schema(description = "Identificador de la sede", example = "1")
    private Long id;

    @Schema(description = "Nombre de la sede", example = "Movistar Arena")
    private String nombre;

    @Schema(description = "Dirección completa", example = "Diagonal 61C # 26-36")
    private String direccion;

    @Schema(description = "Capacidad máxima de aforo", example = "14000")
    private Integer capacidad;

    @Schema(description = "Ciudad donde está ubicada", example = "Bogotá")
    private String city;

    public String getCiudad() {
        return city;
    }

    public void setCiudad(String ciudad) {
        this.city = ciudad;
    }
}
