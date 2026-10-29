package com.eventify.dto;

import com.eventify.dto.validation.OnUpdate;
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
@Schema(description = "Datos para actualizar una sede existente")
public class VenueUpdateDTO {

    @Schema(description = "Identificador de la sede a actualizar", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID de la sede es obligatorio para la actualización", groups = OnUpdate.class)
    @Min(value = 1, message = "El ID de la sede debe ser mayor a 0", groups = OnUpdate.class)
    private Long id;

    @Schema(description = "Nombre actualizado de la sede", example = "Movistar Arena Bogotá", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El nombre de la sede es obligatorio", groups = {OnUpdate.class, Default.class})
    @Size(min = 3, max = 150, message = "El nombre debe contener entre 3 y 150 caracteres", groups = {OnUpdate.class, Default.class})
    private String nombre;

    @Schema(description = "Dirección física actualizada", example = "Diagonal 61C # 26-36", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La dirección de la sede es obligatoria", groups = {OnUpdate.class, Default.class})
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres", groups = {OnUpdate.class, Default.class})
    private String direccion;

    @Schema(description = "Capacidad máxima actualizada", example = "15000", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "La capacidad es obligatoria", groups = {OnUpdate.class, Default.class})
    @Min(value = 1, message = "La capacidad debe ser mayor a 0", groups = {OnUpdate.class, Default.class})
    private Integer capacidad;

    @Schema(description = "Ciudad de la sede", example = "Bogotá", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "La ciudad de la sede es obligatoria", groups = {OnUpdate.class, Default.class})
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres", groups = {OnUpdate.class, Default.class})
    private String city;

    public String getCiudad() {
        return city;
    }

    public void setCiudad(String ciudad) {
        this.city = ciudad;
    }
}
