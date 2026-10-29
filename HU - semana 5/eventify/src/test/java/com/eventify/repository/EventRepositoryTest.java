package com.eventify.repository;

import com.eventify.model.Event;
import com.eventify.model.Venue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    private Venue createTestVenue() {
        Venue venue = new Venue(null, "Centro Ágora", "Calle 24 # 38", 4000, "Bogotá");
        return venueRepository.save(venue);
    }

    @Test
    @DisplayName("Debe persistir un evento con sede y encontrarlo por Derived Query con coincidencia parcial")
    void shouldSaveAndFindByNombreContainingIgnoreCase() {
        // Arrange
        Venue venue = createTestVenue();
        Event event = new Event(null, "Conferencia de Spring Boot Avanzado", LocalDate.now(), "Taller práctico", venue);
        eventRepository.save(event);

        // Act
        List<Event> result = eventRepository.findByNombreContainingIgnoreCase("spring");

        // Assert
        assertFalse(result.isEmpty());
        assertTrue(result.stream().anyMatch(e -> e.getNombre().equals("Conferencia de Spring Boot Avanzado")),
                "Debe encontrar el evento específico guardado en la prueba");
    }

    @Test
    @DisplayName("Debe paginar correctamente los eventos")
    void shouldPaginateEvents() {
        // Arrange
        Venue venue = createTestVenue();
        eventRepository.save(new Event(null, "Evento Test 1", LocalDate.now(), "D1", venue));
        eventRepository.save(new Event(null, "Evento Test 2", LocalDate.now(), "D2", venue));
        eventRepository.save(new Event(null, "Evento Test 3", LocalDate.now(), "D3", venue));

        // Act
        Page<Event> page = eventRepository.findAll(PageRequest.of(0, 2));

        // Assert
        assertEquals(2, page.getContent().size());
        assertTrue(page.getTotalElements() >= 3);
    }
}
