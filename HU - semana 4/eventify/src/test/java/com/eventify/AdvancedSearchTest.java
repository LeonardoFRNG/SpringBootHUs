package com.eventify;

import com.eventify.dto.EventSummaryDTO;
import com.eventify.model.Category;
import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.repository.CategoryRepository;
import com.eventify.repository.EventRepository;
import com.eventify.repository.VenueRepository;
import com.eventify.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdvancedSearchTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Búsqueda 1: Búsqueda flexible por 'bog' debe encontrar eventos en 'Bogotá'")
    void search1_partialCitySearchShouldFindBogota() {
        // En los datos de prueba de V3 existen sedes en Bogotá
        Slice<EventSummaryDTO> slice = eventService.searchEventsSummary(
                null, "bog", null, null, null, null, PageRequest.of(0, 20)
        );

        assertFalse(slice.isEmpty(), "La búsqueda parcial por 'bog' debe encontrar eventos");
        assertTrue(slice.getContent().stream().allMatch(e -> e.venueCity().equalsIgnoreCase("Bogotá")),
                "Todos los resultados deben pertenecer a Bogotá");
    }

    @Test
    @DisplayName("Búsqueda 2: Búsqueda insensible 'rock' debe encontrar eventos con 'ROCK' en mayúsculas")
    void search2_caseInsensitiveSearchShouldFindRockInUppercase() {
        Slice<EventSummaryDTO> slice = eventService.searchEventsSummary(
                "rock", null, null, null, null, null, PageRequest.of(0, 20)
        );

        assertFalse(slice.isEmpty(), "La búsqueda por 'rock' debe encontrar eventos de rock");
        assertTrue(slice.getContent().stream().anyMatch(e -> e.name().contains("ROCK") || e.name().toLowerCase().contains("rock")),
                "Debe encontrar eventos con 'ROCK' independientemente de mayúsculas o minúsculas");
    }

    @Test
    @DisplayName("Búsqueda 3: Búsqueda combinada por ciudad, categoría y rango de fechas")
    void search3_combinedFiltersSearch() {
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2027, 12, 31);

        Slice<EventSummaryDTO> slice = eventService.searchEventsSummary(
                null, "Medellín", "Sports", null, start, end, PageRequest.of(0, 20)
        );

        // Si existen resultados, verificamos que satisfagan todas las condiciones simultáneamente
        for (EventSummaryDTO dto : slice.getContent()) {
            assertEquals("Medellín", dto.venueCity());
            assertTrue(dto.date().compareTo(start) >= 0 && dto.date().compareTo(end) <= 0);
            assertTrue(dto.categories().contains("Sports"));
        }
    }

    @Test
    @DisplayName("Búsqueda 4: Ordenamiento cronológico descendente estable (fecha DESC, id DESC)")
    void search4_shouldSortByDateDescendingAndIdDescending() {
        Slice<EventSummaryDTO> slice = eventService.searchEventsSummary(
                null, null, null, null, null, null, PageRequest.of(0, 30)
        );

        List<EventSummaryDTO> content = slice.getContent();
        assertTrue(content.size() >= 2);

        for (int i = 0; i < content.size() - 1; i++) {
            EventSummaryDTO current = content.get(i);
            EventSummaryDTO next = content.get(i + 1);

            assertTrue(current.date().compareTo(next.date()) >= 0,
                    "Las fechas deben estar ordenadas descendentemente");
            if (current.date().isEqual(next.date())) {
                assertTrue(current.id() > next.id(),
                        "A fechas iguales, el ID debe desempatar de forma descendente y estable");
            }
        }
    }

    @Test
    @DisplayName("Búsqueda 5: Paginación Slice sin duplicaciones entre páginas")
    void search5_slicePaginationWithoutDuplicates() {
        Slice<EventSummaryDTO> page0 = eventService.searchEventsSummary(
                null, null, null, null, null, null, PageRequest.of(0, 10)
        );
        Slice<EventSummaryDTO> page1 = eventService.searchEventsSummary(
                null, null, null, null, null, null, PageRequest.of(1, 10)
        );

        assertEquals(10, page0.getContent().size());
        assertEquals(10, page1.getContent().size());

        List<Long> idsPage0 = page0.getContent().stream().map(EventSummaryDTO::id).toList();
        List<Long> idsPage1 = page1.getContent().stream().map(EventSummaryDTO::id).toList();

        // No debe haber intersección entre páginas
        for (Long id : idsPage0) {
            assertFalse(idsPage1.contains(id), "No debe haber duplicados entre páginas continuas de Slice");
        }
    }

    @Test
    @DisplayName("Búsqueda 6: Exclusión estricta de eventos inactivos de los resultados del catálogo")
    void search6_shouldExcludeInactiveEvents() {
        Venue venue = venueRepository.save(new Venue(null, "Sede Test", "Dir", 1000, "Armenia"));
        Event inactiveEvent = new Event(null, "Evento Inactivo Especial", LocalDate.of(2028, 1, 1), "Inactivo", venue);
        inactiveEvent.setActive(false);
        eventRepository.save(inactiveEvent);

        Slice<EventSummaryDTO> slice = eventService.searchEventsSummary(
                "Inactivo Especial", null, null, null, null, null, PageRequest.of(0, 10)
        );

        assertTrue(slice.isEmpty(), "El catálogo y búsqueda deben excluir automáticamente registros con active=false");
    }

    @Test
    @DisplayName("Búsqueda 7: Validación de rangos de fechas inválidos (startDate posterior a endDate)")
    void search7_shouldThrowExceptionWhenStartDateIsAfterEndDate() {
        LocalDate start = LocalDate.of(2026, 12, 31);
        LocalDate end = LocalDate.of(2026, 1, 1);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.searchEventsSummary(null, null, null, null, start, end, PageRequest.of(0, 10))
        );

        assertTrue(ex.getMessage().contains("posterior a la fecha final"));
    }

    @Test
    @DisplayName("Búsqueda 8: Parámetros vacíos no deben impedir la ejecución de la búsqueda")
    void search8_emptyParametersShouldReturnValidResults() {
        Slice<EventSummaryDTO> slice = eventService.searchEventsSummary(
                "   ", "   ", "   ", null, null, null, PageRequest.of(0, 15)
        );

        assertFalse(slice.isEmpty(), "Filtros con espacios en blanco deben normalizarse a nulo y retornar datos");
        assertEquals(15, slice.getContent().size());
    }
}
