package com.eventify.service;

import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.repository.EventRepository;
import com.eventify.repository.VenueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("Debe guardar un evento válido exitosamente asociando su sede")
    void shouldSaveValidEvent() {
        Venue venue = new Venue(1L, "Movistar Arena", "Diag 61C", 14000, "Bogotá");
        Event eventInput = new Event(null, "Concierto de Jazz", LocalDate.now(), "Música en vivo", venue);
        Event eventSaved = new Event(1L, "Concierto de Jazz", LocalDate.now(), "Música en vivo", venue);

        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenReturn(eventSaved);

        Event result = eventService.create(eventInput);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Concierto de Jazz", result.getNombre());
        assertTrue(result.isActive(), "El evento nuevo debe quedar activo por defecto");
        verify(eventRepository, times(1)).save(eventInput);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el nombre es nulo o vacío")
    void shouldThrowExceptionWhenNameIsInvalid() {
        Venue venue = new Venue(1L, "Movistar Arena", "Diag 61C", 14000, "Bogotá");
        Event eventEmpty = new Event(null, "   ", LocalDate.now(), "Sin nombre", venue);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.create(eventEmpty)
        );

        assertEquals("El nombre del evento es obligatorio", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el evento no tiene sede")
    void shouldThrowExceptionWhenVenueIsNull() {
        Event eventWithoutVenue = new Event(null, "Festival", LocalDate.now(), "Sin sede", null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.create(eventWithoutVenue)
        );

        assertEquals("El evento debe estar asociado a una sede (venue)", exception.getMessage());
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Debe aplicar borrado lógico cambiando active a false sin eliminar físicamente")
    void shouldSoftDeleteEventWithoutPhysicalDelete() {
        Venue venue = new Venue(1L, "Sede", "Dir", 1000, "Cali");
        Event event = new Event(1L, "Evento Existente", LocalDate.now(), "Desc", venue);
        assertTrue(event.isActive());

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        eventService.softDelete(1L);

        assertFalse(event.isActive(), "El evento debe haber quedado inactivo tras el borrado lógico");
        verify(eventRepository, times(1)).save(event);
        verify(eventRepository, never()).delete(any(Event.class));
    }

    @Test
    @DisplayName("Debe retornar la lista de eventos provista por el repositorio")
    void shouldReturnAllEvents() {
        List<Event> mockList = List.of(
                new Event(1L, "Evento A", LocalDate.now(), "Desc A"),
                new Event(2L, "Evento B", LocalDate.now(), "Desc B")
        );
        when(eventRepository.findAll()).thenReturn(mockList);

        List<Event> result = eventService.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(eventRepository, times(1)).findAll();
    }
}
