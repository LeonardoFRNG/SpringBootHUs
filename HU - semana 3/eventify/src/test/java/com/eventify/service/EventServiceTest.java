package com.eventify.service;

import com.eventify.model.Event;
import com.eventify.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("Debe guardar un evento válido exitosamente")
    void shouldSaveValidEvent() {
        // 1. Arrange (Preparar)
        Event eventInput = new Event(null, "Concierto de Jazz", LocalDate.now(), "Música en vivo");
        Event eventSaved = new Event(1L, "Concierto de Jazz", LocalDate.now(), "Música en vivo");

        when(eventRepository.save(any(Event.class))).thenReturn(eventSaved);

        // 2. Act (Actuar)
        Event result = eventService.create(eventInput);

        // 3. Assert (Verificar)
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Concierto de Jazz", result.getNombre());
        verify(eventRepository, times(1)).save(eventInput);
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando el nombre es nulo o vacío")
    void shouldThrowExceptionWhenNameIsInvalid() {
        // 1. Arrange
        Event eventEmpty = new Event(null, "   ", LocalDate.now(), "Sin nombre");

        // 2. Act & Assert combinados mediante assertThrows
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.create(eventEmpty)
        );

        assertEquals("El nombre del evento es obligatorio", exception.getMessage());
        // Verificación clave: el repositorio NUNCA debió ser invocado
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    @DisplayName("Debe retornar la lista de eventos provista por el repositorio")
    void shouldReturnAllEvents() {
        // 1. Arrange
        List<Event> mockList = List.of(
                new Event(1L, "Evento A", LocalDate.now(), "Desc A"),
                new Event(2L, "Evento B", LocalDate.now(), "Desc B")
        );
        when(eventRepository.findAll()).thenReturn(mockList);

        // 2. Act
        List<Event> result = eventService.getAll();

        // 3. Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(eventRepository, times(1)).findAll();
    }
}