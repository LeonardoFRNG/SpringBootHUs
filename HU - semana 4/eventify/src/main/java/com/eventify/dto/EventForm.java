package com.eventify.dto;

import com.eventify.model.Category;
import com.eventify.model.Event;
import com.eventify.model.Venue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Form Backing Object para la creación y edición de eventos en el panel Thymeleaf.
 * Facilita el enlace de datos, validación y selección múltiple de categorías.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventForm {

    private Long id;

    @NotBlank(message = "El nombre del evento es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    private String nombre;

    @NotNull(message = "La fecha del evento es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "La selección de sede (venue) es obligatoria")
    private Long venueId;

    private List<Long> categoryIds = new ArrayList<>();

    public static EventForm fromEntity(Event event) {
        EventForm form = new EventForm();
        form.setId(event.getId());
        form.setNombre(event.getNombre());
        form.setFecha(event.getFecha());
        form.setDescripcion(event.getDescripcion());
        if (event.getVenue() != null) {
            form.setVenueId(event.getVenue().getId());
        }
        if (event.getCategories() != null) {
            form.setCategoryIds(event.getCategories().stream().map(Category::getId).collect(Collectors.toList()));
        }
        return form;
    }

    public Event toEntity(Venue venue, Set<Category> categories) {
        Event event = new Event();
        event.setId(this.id);
        event.setNombre(this.nombre);
        event.setFecha(this.fecha);
        event.setDescripcion(this.descripcion);
        event.setActive(true);
        event.setVenue(venue);
        event.setCategories(categories != null ? categories : new HashSet<>());
        return event;
    }
}
