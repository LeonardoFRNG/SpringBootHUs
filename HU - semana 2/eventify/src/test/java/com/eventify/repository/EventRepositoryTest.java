package com.eventify.repository;

import com.eventify.model.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("Debe persistir un evento y encontrarlo por Derived Query con coincidencia parcial")
    void shouldSaveAndFindByNombreContainingIgnoreCase() {
        // Arrange
        Event event = new Event(null, "Conferencia de Spring Boot", LocalDate.now(), "Taller práctico");
        eventRepository.save(event);

        // Act
        List<Event> result = eventRepository.findByNombreContainingIgnoreCase("spring");

        // Assert
        assertFalse(result.isEmpty());
        assertEquals("Conferencia de Spring Boot", result.get(0).getNombre());
    }

    @Test
    @DisplayName("Debe paginar correctamente los eventos")
    void shouldPaginateEvents() {
        // Arrange: Guardamos 3 eventos
        eventRepository.save(new Event(null, "Evento 1", LocalDate.now(), "D1"));
        eventRepository.save(new Event(null, "Evento 2", LocalDate.now(), "D2"));
        eventRepository.save(new Event(null, "Evento 3", LocalDate.now(), "D3"));

        // Act: Página 0 con tamaño 2
        Page<Event> page = eventRepository.findAll(PageRequest.of(0, 2));

        // Assert
        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals(2, page.getTotalPages());
    }
}