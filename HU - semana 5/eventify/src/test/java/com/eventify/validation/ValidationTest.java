package com.eventify.validation;

import com.eventify.dto.CategoryCreateDTO;
import com.eventify.dto.EventCreateDTO;
import com.eventify.dto.EventUpdateDTO;
import com.eventify.dto.VenueCreateDTO;
import com.eventify.dto.validation.OnCreate;
import com.eventify.dto.validation.OnUpdate;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // =========================================================================
    // Pruebas del validador personalizado @NoPastEvents
    // =========================================================================

    @Test
    @DisplayName("@NoPastEvents: Debe rechazar una fecha anterior a hoy (pasada)")
    void shouldRejectPastDate() {
        EventCreateDTO dto = EventCreateDTO.builder()
                .nombre("Evento Pasado")
                .fecha(LocalDate.now().minusDays(1)) // Ayer
                .venueId(1L)
                .build();

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto, OnCreate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("fecha") &&
                v.getMessage().contains("no puede ser anterior a hoy")));
    }

    @Test
    @DisplayName("@NoPastEvents: Debe aceptar la fecha de hoy (presente)")
    void shouldAcceptTodayDate() {
        EventCreateDTO dto = EventCreateDTO.builder()
                .nombre("Evento de Hoy")
                .fecha(LocalDate.now()) // Hoy
                .venueId(1L)
                .build();

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto, OnCreate.class);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("fecha")),
                "La fecha de hoy debe ser plenamente válida");
    }

    @Test
    @DisplayName("@NoPastEvents: Debe aceptar una fecha futura")
    void shouldAcceptFutureDate() {
        EventCreateDTO dto = EventCreateDTO.builder()
                .nombre("Evento Futuro")
                .fecha(LocalDate.now().plusMonths(6)) // Futuro
                .venueId(1L)
                .build();

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto, OnCreate.class);
        assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("fecha")));
    }

    // =========================================================================
    // Pruebas de validación en EventCreateDTO
    // =========================================================================

    @Test
    @DisplayName("EventCreateDTO: Debe fallar si el nombre está en blanco o tiene menos de 3 caracteres")
    void shouldFailWhenEventNameIsBlankOrTooShort() {
        EventCreateDTO blankDto = EventCreateDTO.builder()
                .nombre("  ")
                .fecha(LocalDate.now().plusDays(1))
                .venueId(1L)
                .build();

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(blankDto, OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));

        EventCreateDTO shortDto = EventCreateDTO.builder()
                .nombre("AB")
                .fecha(LocalDate.now().plusDays(1))
                .venueId(1L)
                .build();

        Set<ConstraintViolation<EventCreateDTO>> shortViolations = validator.validate(shortDto, OnCreate.class);
        assertTrue(shortViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nombre") &&
                v.getMessage().contains("entre 3 y 100 caracteres")));
    }

    @Test
    @DisplayName("EventCreateDTO: Debe fallar si el nombre supera los 100 caracteres")
    void shouldFailWhenEventNameExceeds100Chars() {
        EventCreateDTO dto = EventCreateDTO.builder()
                .nombre("A".repeat(101))
                .fecha(LocalDate.now().plusDays(1))
                .venueId(1L)
                .build();

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(dto, OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nombre") &&
                v.getMessage().contains("entre 3 y 100 caracteres")));
    }

    @Test
    @DisplayName("EventCreateDTO: Debe fallar si venueId es nulo o menor o igual a cero")
    void shouldFailWhenVenueIdIsMissingOrInvalid() {
        EventCreateDTO nullVenue = EventCreateDTO.builder()
                .nombre("Concierto Válido")
                .fecha(LocalDate.now().plusDays(5))
                .venueId(null)
                .build();

        Set<ConstraintViolation<EventCreateDTO>> violations = validator.validate(nullVenue, OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("venueId")));

        EventCreateDTO zeroVenue = EventCreateDTO.builder()
                .nombre("Concierto Válido")
                .fecha(LocalDate.now().plusDays(5))
                .venueId(0L)
                .build();

        Set<ConstraintViolation<EventCreateDTO>> zeroViolations = validator.validate(zeroVenue, OnCreate.class);
        assertTrue(zeroViolations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("venueId")));
    }

    // =========================================================================
    // Pruebas de Grupos de Validación OnCreate vs OnUpdate
    // =========================================================================

    @Test
    @DisplayName("Grupos de Validación: EventUpdateDTO requiere 'id' bajo grupo OnUpdate")
    void shouldRequireIdForUpdateGroup() {
        EventUpdateDTO dtoWithoutId = EventUpdateDTO.builder()
                .id(null)
                .nombre("Actualización Válida")
                .fecha(LocalDate.now().plusDays(2))
                .venueId(1L)
                .build();

        Set<ConstraintViolation<EventUpdateDTO>> violations = validator.validate(dtoWithoutId, OnUpdate.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("id") &&
                v.getMessage().contains("obligatorio para la actualización")));
    }

    @Test
    @DisplayName("Grupos de Validación: EventUpdateDTO con ID válido no genera violaciones en grupo OnUpdate")
    void shouldPassEventUpdateWithValidId() {
        EventUpdateDTO dtoWithId = EventUpdateDTO.builder()
                .id(10L)
                .nombre("Actualización Válida")
                .fecha(LocalDate.now().plusDays(2))
                .venueId(1L)
                .build();

        Set<ConstraintViolation<EventUpdateDTO>> violations = validator.validate(dtoWithId, OnUpdate.class);
        assertTrue(violations.isEmpty(), "No deben existir violaciones si los datos son válidos");
    }

    // =========================================================================
    // Pruebas de validación en VenueCreateDTO y CategoryCreateDTO
    // =========================================================================

    @Test
    @DisplayName("VenueCreateDTO: Debe validar ciudad, capacidad > 0 y nombre obligatorio")
    void shouldValidateVenueConstraints() {
        VenueCreateDTO invalid = VenueCreateDTO.builder()
                .nombre(" ")
                .direccion("Cra 10")
                .capacidad(0)
                .city(" ")
                .build();

        Set<ConstraintViolation<VenueCreateDTO>> violations = validator.validate(invalid, OnCreate.class);
        assertTrue(violations.size() >= 3);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nombre")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("capacidad")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("city")));
    }

    @Test
    @DisplayName("CategoryCreateDTO: Debe validar nombre entre 2 y 100 caracteres")
    void shouldValidateCategoryConstraints() {
        CategoryCreateDTO shortCategory = CategoryCreateDTO.builder()
                .name("A")
                .build();

        Set<ConstraintViolation<CategoryCreateDTO>> violations = validator.validate(shortCategory, OnCreate.class);
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }
}
