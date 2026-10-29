package com.eventify;

import com.eventify.model.Category;
import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.repository.CategoryRepository;
import com.eventify.repository.EventRepository;
import com.eventify.repository.VenueRepository;
import com.eventify.service.EventService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class DomainAndPersistenceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("Regla 1: Un evento nuevo queda activo por defecto (active = true)")
    void rule1_newEventShouldBeActiveByDefault() {
        Venue venue = venueRepository.save(new Venue(null, "Teatro Colón", "Calle 10 # 5-32", 900, "Bogotá"));
        Event event = new Event(null, "Concierto Barroco", LocalDate.now(), "Música clásica", venue);

        Event saved = eventService.create(event);

        assertNotNull(saved.getId());
        assertTrue(saved.isActive(), "El evento debe estar activo por defecto");
    }

    @Test
    @DisplayName("Regla 2: No se puede guardar un evento sin una sede válida")
    void rule2_shouldRejectEventWithoutValidVenue() {
        Event eventWithoutVenue = new Event(null, "Feria Sin Sede", LocalDate.now(), "Sin sede", null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.create(eventWithoutVenue)
        );

        assertTrue(ex.getMessage().contains("sede"));
    }

    @Test
    @DisplayName("Regla 3 & 4: Relación ManyToMany bidireccional (Event <-> Category)")
    void rule3and4_shouldSupportManyToManyBetweenEventAndCategories() {
        Venue venue = venueRepository.save(new Venue(null, "Arena Medellín", "Calle 44", 8000, "Medellín"));

        Category catConcerts = categoryRepository.findByNameIgnoreCase("Concerts")
                .orElseGet(() -> categoryRepository.save(new Category(null, "Concerts", "Música")));
        Category catFestivals = categoryRepository.findByNameIgnoreCase("Festivals")
                .orElseGet(() -> categoryRepository.save(new Category(null, "Festivals", "Festivales")));

        // 1 Evento con múltiples categorías
        Event event1 = new Event(null, "Festival Musical Estéreo", LocalDate.now(), "Festival y concierto", venue, Set.of(catConcerts, catFestivals));
        eventRepository.save(event1);

        // Otro Evento compartiendo la misma categoría Concerts
        Event event2 = new Event(null, "Noche Acústica", LocalDate.now().plusDays(1), "Acústico", venue, Set.of(catConcerts));
        eventRepository.save(event2);

        entityManager.flush();
        entityManager.clear();

        Event loaded1 = eventRepository.findByIdWithRelations(event1.getId()).orElseThrow();
        assertEquals(2, loaded1.getCategories().size(), "El evento 1 debe tener 2 categorías asociadas");

        Event loaded2 = eventRepository.findByIdWithRelations(event2.getId()).orElseThrow();
        assertEquals(1, loaded2.getCategories().size(), "El evento 2 debe tener 1 categoría asociada");
    }

    @Test
    @DisplayName("Regla 5 & 6: Desactivar un evento (borrado lógico) no elimina el registro físico y lo oculta de listados normales")
    void rule5and6_softDeleteShouldDeactivateWithoutPhysicalDelete() {
        Venue venue = venueRepository.save(new Venue(null, "Plaza de Toros", "Cra 1", 5000, "Cali"));
        Event event = eventRepository.save(new Event(null, "Evento a Desactivar", LocalDate.now(), "Prueba soft delete", venue));
        Long eventId = event.getId();

        // Aplicamos borrado lógico
        eventService.softDelete(eventId);
        entityManager.flush();
        entityManager.clear();

        // 1. Las consultas normales JPA con @SQLRestriction no deben encontrarlo
        assertTrue(eventRepository.findById(eventId).isEmpty(), "Consultas normales deben excluir el evento inactivo");
        List<Event> allActive = eventRepository.findAll();
        assertFalse(allActive.stream().anyMatch(e -> e.getId().equals(eventId)), "findAll() no debe contener el evento inactivo");

        // 2. Consulta de auditoría nativa sí debe encontrarlo en la base de datos física
        List<Event> inactives = eventRepository.findInactiveEventsForAudit();
        assertTrue(inactives.stream().anyMatch(e -> e.getId().equals(eventId)), "El registro debe conservarse físicamente en la BD");
    }

    @Test
    @DisplayName("Regla 7: Las sedes y categorías asociadas NO se eliminan al desactivar o eliminar un evento")
    void rule7_softDeleteMustNotDeleteSharedVenueOrCategories() {
        Venue venue = venueRepository.save(new Venue(null, "Auditorio Compartido", "Calle 100", 1200, "Bogotá"));
        Category category = categoryRepository.findByNameIgnoreCase("Workshops")
                .orElseGet(() -> categoryRepository.save(new Category(null, "Workshops", "Talleres")));

        Event event = eventRepository.save(new Event(null, "Taller de Microservicios", LocalDate.now(), "Taller", venue, Set.of(category)));
        Long eventId = event.getId();
        Long venueId = venue.getId();
        Long catId = category.getId();

        // Desactivamos el evento
        eventService.softDelete(eventId);
        entityManager.flush();
        entityManager.clear();

        // La sede y la categoría deben seguir existiendo intactas en la base de datos
        assertTrue(venueRepository.findById(venueId).isPresent(), "La sede compartida NO debe ser eliminada");
        assertTrue(categoryRepository.findById(catId).isPresent(), "La categoría compartida NO debe ser eliminada");
    }
}
