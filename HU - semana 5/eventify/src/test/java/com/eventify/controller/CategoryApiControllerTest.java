package com.eventify.controller;

import com.eventify.dto.CategoryCreateDTO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class CategoryApiControllerTest {

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
    @DisplayName("API Categorías: Creación y consulta desacoplada con CategoryResponseDTO")
    void shouldCreateAndRetrieveCategories() throws Exception {
        String uniqueName = "Cat_" + UUID.randomUUID().toString().substring(0, 8);
        CategoryCreateDTO createDTO = CategoryCreateDTO.builder()
                .name(uniqueName)
                .description("Descripción de la categoría de prueba")
                .build();

        // 1. POST /api/categories (201 Created)
        String responseContent = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(uniqueName))
                .andReturn().getResponse().getContentAsString();

        Number catIdNum = com.jayway.jsonpath.JsonPath.read(responseContent, "$.id");
        Long categoryId = catIdNum.longValue();

        // 2. GET /api/categories/{id} (200 OK)
        mockMvc.perform(get("/api/categories/" + categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value(uniqueName))
                .andExpect(jsonPath("$.description").value("Descripción de la categoría de prueba"));

        // 3. GET /api/categories (200 OK)
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[*].name", hasItem(uniqueName)));
    }
}
