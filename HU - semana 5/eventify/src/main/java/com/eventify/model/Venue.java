package com.eventify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del lugar es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    @Column(nullable = false, length = 200)
    private String direccion;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Column(nullable = false)
    private Integer capacidad;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
    @Column(nullable = false, length = 100)
    private String city;

    /**
     * Constructor de compatibilidad histórica (Semana 3).
     * Asigna una ciudad por defecto si no es provista.
     */
    public Venue(Long id, String nombre, String direccion, Integer capacidad) {
        this(id, nombre, direccion, capacidad, "Barranquilla");
    }

    // Alias para conveniencia y coherencia en idioma español
    public String getCiudad() {
        return city;
    }

    public void setCiudad(String ciudad) {
        this.city = ciudad;
    }
}
