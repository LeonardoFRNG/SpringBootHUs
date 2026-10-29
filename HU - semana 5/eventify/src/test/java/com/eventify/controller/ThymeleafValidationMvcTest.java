package com.eventify.controller;

import com.eventify.model.Venue;
import com.eventify.repository.VenueRepository;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class ThymeleafValidationMvcTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private VenueRepository venueRepository;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Thymeleaf Form: Envío con errores de validación re-renderiza el formulario con clase .campo-invalido y conserva valores")
    void shouldReRenderFormWithValidationErrorsAndStyles() throws Exception {
        mockMvc.perform(post("/admin/events")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nombre", "   ") // Inválido: en blanco
                        .param("fecha", LocalDate.now().minusDays(10).toString()) // Inválido: fecha pasada (@NoPastEvents)
                        .param("descripcion", "Descripción que debe conservarse tras el fallo")
                        // venueId omitido deliberadamente
                )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/event-form"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasFieldErrors("event", "nombre", "fecha", "venueId"))
                // Verificación de feedback visual en el HTML renderizado
                .andExpect(content().string(containsString("campo-invalido")))
                .andExpect(content().string(containsString("is-invalid")))
                // Verificación de mensajes de error específicos
                .andExpect(content().string(containsString("El nombre del evento es obligatorio")))
                .andExpect(content().string(containsString("La fecha del evento no puede ser anterior a hoy")))
                .andExpect(content().string(containsString("La selección de sede (venue) es obligatoria")))
                // Verificación de conservación de valores ingresados
                .andExpect(content().string(containsString("que debe conservarse tras el fallo")));
    }

    @Test
    @DisplayName("Thymeleaf Form: Registro exitoso redirige con mensaje flash 'successMessage'")
    void shouldRedirectWithSuccessFlashMessage() throws Exception {
        Venue venue = venueRepository.findAll().get(0);

        mockMvc.perform(post("/admin/events")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nombre", "Conferencia Web Spring 2026")
                        .param("fecha", LocalDate.now().plusMonths(1).toString())
                        .param("descripcion", "Evento presencial de actualización")
                        .param("venueId", venue.getId().toString())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", containsString("registrado exitosamente")));
    }

    @Test
    @DisplayName("Thymeleaf Venue Form: Envío inválido re-renderiza con estilos de error y feedback visual")
    void shouldReRenderVenueFormWithValidationErrors() throws Exception {
        mockMvc.perform(post("/admin/venues")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nombre", "") // Inválido
                        .param("city", "")   // Inválido
                        .param("direccion", "") // Inválido
                        .param("capacidad", "0") // Inválido
                )
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venue-form"))
                .andExpect(model().hasErrors())
                .andExpect(content().string(containsString("campo-invalido")))
                .andExpect(content().string(containsString("is-invalid")));
    }
}
