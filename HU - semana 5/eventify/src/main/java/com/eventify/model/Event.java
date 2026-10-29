package com.eventify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entidad Event con soporte para:
 * - Borrado lógico mediante el atributo 'active' y la anotación Hibernate @SQLRestriction.
 * - Relación ManyToOne obligatoria con Venue.
 * - Relación ManyToMany con Category mediante la tabla intermedia 'events_categories'.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
@SQLRestriction("active = true")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(nullable = false, length = 150)
    private String nombre;

    @NotNull(message = "La fecha del evento es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(nullable = false)
    private LocalDate fecha;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private boolean active = true;

    @NotNull(message = "El evento debe estar asociado a una sede (venue)")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "events_categories",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    /**
     * Constructor de compatibilidad histórica (Semana 3).
     */
    public Event(Long id, String nombre, LocalDate fecha, String descripcion) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.active = true;
        this.categories = new HashSet<>();
    }

    public Event(Long id, String nombre, LocalDate fecha, String descripcion, Venue venue) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.active = true;
        this.venue = venue;
        this.categories = new HashSet<>();
    }

    public Event(Long id, String nombre, LocalDate fecha, String descripcion, Venue venue, Set<Category> categories) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.active = true;
        this.venue = venue;
        this.categories = categories != null ? categories : new HashSet<>();
    }

    /**
     * Ejecuta el borrado lógico del evento, cambiando su estado a inactivo (active = false).
     * No elimina físicamente el registro de la base de datos ni elimina sedes o categorías asociadas.
     */
    public void softDelete() {
        this.active = false;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void addCategory(Category category) {
        if (category != null) {
            this.categories.add(category);
        }
    }

    public void removeCategory(Category category) {
        if (category != null) {
            this.categories.remove(category);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
