package com.eventify.dto;

import com.eventify.dto.validation.OnCreate;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos para el registro de una nueva sede o lugar")
public class VenueCreateDTO {

    @Schema(description = "Nombre o denominación de la sede", example = "Movistar Arena", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre de la sede es obligatorio", groups = {OnCreate.class, Default.class})
    @Size(min = 3, max = 150, message = "El nombre debe contener entre 3 y 150 caracteres", groups = {OnCreate.class, Default.class})
    private String nombre;

    @Schema(description = "Dirección física completa", example = "Diagonal 61C # 26-36", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La dirección de la sede es obligatoria", groups = {OnCreate.class, Default.class})
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres", groups = {OnCreate.class, Default.class})
    private String direccion;

    @Schema(description = "Aforo máximo de personas permitido", example = "14000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La capacidad es obligatoria", groups = {OnCreate.class, Default.class})
    @Min(value = 1, message = "La capacidad debe ser mayor a 0", groups = {OnCreate.class, Default.class})
    private Integer capacidad;

    @Schema(description = "Ciudad donde se ubica la sede", example = "Bogotá", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La ciudad de la sede es obligatoria", groups = {OnCreate.class, Default.class})
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres", groups = {OnCreate.class, Default.class})
    private String city;

    public String getCiudad() {
        return city;
    }

    public void setCiudad(String ciudad) {
        this.city = ciudad;
    }
}
