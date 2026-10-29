package com.eventify.controller;

import com.eventify.model.Category;
import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.repository.CategoryRepository;
import com.eventify.repository.EventRepository;
import com.eventify.repository.VenueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class EventApiControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("API 1: GET /api/events - Retorna Slice con EventSummaryDTO, venueName, venueCity y categories")
    void api1_shouldReturnSliceOfEventSummaryDTO() throws Exception {
        mockMvc.perform(get("/api/events")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.content[0].name").exists())
                .andExpect(jsonPath("$.content[0].date").exists())
                .andExpect(jsonPath("$.content[0].venueName").exists())
                .andExpect(jsonPath("$.content[0].venueCity").exists())
                .andExpect(jsonPath("$.content[0].categories").isArray())
                // Estructura Slice: no incluye totalPages ni totalElements
                .andExpect(jsonPath("$.first").isBoolean())
                .andExpect(jsonPath("$.numberOfElements").value(5));
    }

    @Test
    @DisplayName("API 2: GET /api/events?city=bog - Filtro por ciudad insensible y parcial")
    void api2_shouldFilterEventsByCity() throws Exception {
        mockMvc.perform(get("/api/events")
                        .param("city", "bog")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[*].venueCity", everyItem(equalToIgnoringCase("Bogotá"))));
    }

    @Test
    @DisplayName("API 3: GET /api/events?name=ROCK - Filtro por nombre insensible")
    void api3_shouldFilterEventsByNameCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/events")
                        .param("name", "ROCK")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())))
                .andExpect(jsonPath("$.content[0].name", containsStringIgnoringCase("rock")));
    }

    @Test
    @DisplayName("API 4: POST /api/events - Registro exitoso con sede obligatoria (201 Created)")
    void api4_shouldCreateEventWithVenue() throws Exception {
        Venue venue = venueRepository.save(new Venue(null, "Centro Ágora", "Cra 38", 3000, "Bogotá"));
        Category category = categoryRepository.findAll().get(0);

        Event newEvent = new Event(null, "Congreso de IA y Datos 2026", LocalDate.of(2026, 11, 20),
                "Conferencia de machine learning", venue, Set.of(category));

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEvent)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Congreso de IA y Datos 2026"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @DisplayName("API 5: DELETE /api/events/{id} - Ejecuta borrado lógico y GET posterior retorna 404")
    void api5_deleteShouldSoftDeleteEvent() throws Exception {
        Venue venue = venueRepository.save(new Venue(null, "Auditorio Sur", "Calle 5", 500, "Cali"));
        Event event = eventRepository.save(new Event(null, "Concierto a Borrar", LocalDate.now(), "Desc", venue));
        Long eventId = event.getId();

        // 1. DELETE /api/events/{id} -> 204 No Content
        mockMvc.perform(delete("/api/events/" + eventId))
                .andExpect(status().isNoContent());

        // 2. GET /api/events/{id} -> 404 Not Found (gracias a @SQLRestriction("active = true"))
        mockMvc.perform(get("/api/events/" + eventId))
                .andExpect(status().isNotFound());

        // 3. GET /api/events/audit/inactive -> Retorna el evento inactivo en auditoría
        mockMvc.perform(get("/api/events/audit/inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(eventId.intValue())));
    }

    @Test
    @DisplayName("API 6: Validación de error temporal (400 Bad Request) si startDate > endDate")
    void api6_shouldReturnBadRequestOnInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/events")
                        .param("startDate", "2026-12-31")
                        .param("endDate", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("posterior a la fecha final")));
    }
}
