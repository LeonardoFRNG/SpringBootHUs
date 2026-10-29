package com.eventify.controller;

import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.repository.EventRepository;
import com.eventify.repository.VenueRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class AdminControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Escenario 1: Visualización Centralizada - Renderiza catálogo optimizado con eventos y lugares")
    void escenario1_shouldRenderAdminPanelWithEventsAndVenues() throws Exception {
        Venue venue = venueRepository.save(new Venue(null, "Centro Puerta de Oro Especial", "Via 40 # 79B", 3500, "Barranquilla"));
        eventRepository.save(new Event(null, "DevFest Caribe Exclusivo 2026", LocalDate.of(2026, 11, 15), "Conferencia Tech", venue));

        mockMvc.perform(get("/admin")
                        .param("name", "DevFest Caribe Exclusivo 2026"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(content().string(containsString("DevFest Caribe Exclusivo 2026")))
                .andExpect(content().string(containsString("Barranquilla")));
    }

    @Test
    @DisplayName("Escenario 2: Gestión de Inventario Vacío - Mensaje amigable cuando filtro no coincide")
    void escenario2_shouldDisplayFriendlyMessageWhenCatalogIsEmpty() throws Exception {
        mockMvc.perform(get("/admin").param("name", "EventoCompletamenteInexistenteXYZ999"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(content().string(containsString("No se encontraron eventos")));
    }

    @Test
    @DisplayName("Escenario 3: Registro y Redirección Exitosa (Evento) con sede obligatoria")
    void escenario3_shouldCreateEventAndRedirectToAdminPanel() throws Exception {
        Venue venue = venueRepository.save(new Venue(null, "Ágora Bogotá Exclusiva", "Calle 24 # 38", 4000, "Bogotá"));

        mockMvc.perform(post("/admin/events")
                        .param("nombre", "Seminario de Cloud Computing 2026")
                        .param("fecha", "2026-12-10")
                        .param("descripcion", "Taller práctico sobre microservicios y despliegue en la nube")
                        .param("venueId", venue.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"));

        assertTrue(eventRepository.findByNombreContainingIgnoreCase("Seminario de Cloud Computing 2026")
                .stream().anyMatch(e -> e.getVenue().getId().equals(venue.getId())));
    }

    @Test
    @DisplayName("Escenario 4: Borrado lógico vía controlador - Desactiva evento y redirecciona")
    void escenario4_shouldDeactivateEventLogically() throws Exception {
        Venue venue = venueRepository.save(new Venue(null, "Movistar Test", "Cra 30", 14000, "Bogotá"));
        Event event = eventRepository.save(new Event(null, "Mega Concierto Desactivable", LocalDate.now(), "Desc", venue));
        Long eventId = event.getId();

        mockMvc.perform(post("/admin/events/" + eventId + "/deactivate"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"));

        entityManager.flush();
        entityManager.clear();

        // El repositorio normal con @SQLRestriction no encuentra el evento
        assertTrue(eventRepository.findById(eventId).isEmpty(), "No debe figurar en búsquedas normales");
        // Pero en auditoría sigue existiendo
        assertTrue(eventRepository.findInactiveEventsForAudit().stream().anyMatch(e -> e.getId().equals(eventId)),
                "Sigue existiendo físicamente en BD");
    }
}
