package com.eventify.exception;

import com.eventify.dto.CategoryCreateDTO;
import com.eventify.dto.EventCreateDTO;
import com.eventify.dto.VenueCreateDTO;
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
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class GlobalExceptionHandlerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("RFC 7807: POST /api/events con datos inválidos retorna 400 ProblemDetail con mapa 'errors'")
    void shouldReturnRfc7807ProblemDetailOnValidationErrors() throws Exception {
        EventCreateDTO invalidDto = EventCreateDTO.builder()
                .nombre("A") // Demasiado corto (<3)
                .fecha(LocalDate.now().minusDays(5)) // Fecha pasada (@NoPastEvents)
                .venueId(null) // Sede obligatoria
                .build();

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Error de validación"))
                .andExpect(jsonPath("$.instance").value("/api/events"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.errors").isMap())
                .andExpect(jsonPath("$.errors.nombre").isNotEmpty())
                .andExpect(jsonPath("$.errors.fecha").value("La fecha del evento no puede ser anterior a hoy"))
                .andExpect(jsonPath("$.errors.venueId").isNotEmpty());
    }

    @Test
    @DisplayName("RFC 7807: GET /api/events/{id} inexistente retorna 404 ProblemDetail estructurado")
    void shouldReturnRfc7807ProblemDetailOnNotFound() throws Exception {
        mockMvc.perform(get("/api/events/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Recurso no encontrado"))
                .andExpect(jsonPath("$.detail", containsString("Evento no encontrado con id: 99999999")))
                .andExpect(jsonPath("$.instance").value("/api/events/99999999"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("RFC 7807: POST /api/venues con nombre duplicado retorna 409 Conflict ProblemDetail")
    void shouldReturnRfc7807ProblemDetailOnDuplicateVenue() throws Exception {
        String uniqueName = "Sede Unica " + UUID.randomUUID();
        VenueCreateDTO firstDto = VenueCreateDTO.builder()
                .nombre(uniqueName)
                .direccion("Calle 100 # 20")
                .capacidad(2500)
                .city("Medellín")
                .build();

        // Primera creación: exitosa
        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstDto)))
                .andExpect(status().isCreated());

        // Segunda creación con el mismo nombre: 409 Conflict
        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstDto)))
                .andExpect(status().isConflict())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflicto de recurso duplicado"))
                .andExpect(jsonPath("$.detail", containsString("Ya existe una sede con el nombre: " + uniqueName)))
                .andExpect(jsonPath("$.instance").value("/api/venues"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    @DisplayName("RFC 7807: POST /api/categories con nombre duplicado retorna 409 Conflict ProblemDetail")
    void shouldReturnRfc7807ProblemDetailOnDuplicateCategory() throws Exception {
        String uniqueCat = "CategoriaUnica" + UUID.randomUUID().toString().substring(0, 6);
        CategoryCreateDTO dto = CategoryCreateDTO.builder()
                .name(uniqueCat)
                .description("Descripción categoría")
                .build();

        // Primera creación: exitosa
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        // Segunda creación: 409 Conflict
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Conflicto de recurso duplicado"))
                .andExpect(jsonPath("$.detail", containsString("Ya existe una categoría con el nombre: " + uniqueCat)))
                .andExpect(jsonPath("$.instance").value("/api/categories"));
    }

    @Test
    @DisplayName("RFC 7807: GET /api/events con startDate > endDate retorna 400 ProblemDetail (BusinessRuleViolation)")
    void shouldReturnRfc7807ProblemDetailOnBusinessRuleViolation() throws Exception {
        mockMvc.perform(get("/api/events")
                        .param("startDate", "2026-12-31")
                        .param("endDate", "2026-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", containsString("application/problem+json")))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Violación de regla de negocio"))
                .andExpect(jsonPath("$.detail", containsString("no puede ser posterior a la fecha final")))
                .andExpect(jsonPath("$.instance").value("/api/events"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
