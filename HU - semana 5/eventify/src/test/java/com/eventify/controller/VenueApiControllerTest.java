package com.eventify.controller;

import com.eventify.dto.VenueCreateDTO;
import com.eventify.dto.VenueUpdateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class VenueApiControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("API Sedes: Ciclo completo de creación, consulta, actualización y eliminación")
    void shouldPerformCrudLifecycleOnVenues() throws Exception {
        String uniqueName = "Arena " + UUID.randomUUID().toString().substring(0, 8);
        VenueCreateDTO createDTO = VenueCreateDTO.builder()
                .nombre(uniqueName)
                .direccion("Avenida Las Vegas 45")
                .capacidad(8500)
                .city("Envigado")
                .build();

        // 1. POST /api/venues (201 Created)
        String responseContent = mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value(uniqueName))
                .andExpect(jsonPath("$.city").value("Envigado"))
                .andReturn().getResponse().getContentAsString();

        Number venueIdNum = com.jayway.jsonpath.JsonPath.read(responseContent, "$.id");
        Long venueId = venueIdNum.longValue();

        // 2. GET /api/venues/{id} (200 OK)
        mockMvc.perform(get("/api/venues/" + venueId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(venueId))
                .andExpect(jsonPath("$.nombre").value(uniqueName));

        // 3. PUT /api/venues/{id} (200 OK)
        VenueUpdateDTO updateDTO = VenueUpdateDTO.builder()
                .id(venueId)
                .nombre(uniqueName + " Modificada")
                .direccion("Avenida Las Vegas 45")
                .capacidad(9000)
                .city("Envigado")
                .build();

        mockMvc.perform(put("/api/venues/" + venueId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value(uniqueName + " Modificada"))
                .andExpect(jsonPath("$.capacidad").value(9000));

        // 4. GET /api/venues/search?city=Envi (200 OK)
        mockMvc.perform(get("/api/venues/search")
                        .param("city", "Envi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].city", hasItem("Envigado")));

        // 5. DELETE /api/venues/{id} (204 No Content)
        mockMvc.perform(delete("/api/venues/" + venueId))
                .andExpect(status().isNoContent());

        // 6. GET /api/venues/{id} posterior retorna 404 ProblemDetail
        mockMvc.perform(get("/api/venues/" + venueId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
