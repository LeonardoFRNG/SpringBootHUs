package com.eventify.service;

import com.eventify.model.Venue;
import com.eventify.repository.VenueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    @Test
    @DisplayName("Debe guardar un venue válido con ciudad exitosamente")
    void shouldSaveValidVenue() {
        Venue venueInput = new Venue(null, "Auditorio Mayor", "Calle 10 # 20", 500, "Bogotá");
        Venue venueSaved = new Venue(1L, "Auditorio Mayor", "Calle 10 # 20", 500, "Bogotá");
        when(venueRepository.save(any(Venue.class))).thenReturn(venueSaved);

        Venue result = venueService.create(venueInput);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(500, result.getCapacidad());
        assertEquals("Bogotá", result.getCity());
        verify(venueRepository).save(venueInput);
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la capacidad es menor o igual a cero")
    void shouldThrowExceptionWhenCapacityIsZeroOrLess() {
        Venue venueInvalid = new Venue(null, "Sala Pequeña", "Cra 15", 0, "Medellín");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> venueService.create(venueInvalid)
        );

        assertEquals("La capacidad debe ser mayor a 0", exception.getMessage());
        verify(venueRepository, never()).save(any(Venue.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la ciudad es nula o vacía")
    void shouldThrowExceptionWhenCityIsBlank() {
        Venue venueWithoutCity = new Venue(null, "Sala Pequeña", "Cra 15", 100, "   ");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> venueService.create(venueWithoutCity)
        );

        assertEquals("La ciudad del venue es obligatoria", exception.getMessage());
        verify(venueRepository, never()).save(any(Venue.class));
    }

    @Test
    @DisplayName("Debe retornar todos los venues del repositorio")
    void shouldReturnAllVenues() {
        when(venueRepository.findAllByOrderByNombreAsc()).thenReturn(List.of(new Venue(1L, "Plaza", "Cra 1", 1000, "Cali")));

        List<Venue> result = venueService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(venueRepository).findAllByOrderByNombreAsc();
    }
}
