package com.eventify.controller;

import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.repository.EventRepository;
import com.eventify.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;
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

    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired(required = false)
    private GroupedOpenApi apiGroup;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        eventRepository.deleteAll();
        venueRepository.deleteAll();
    }

    @Test
    @DisplayName("Escenario 1: Visualización Centralizada (Camino Feliz) - Renderiza tabla con eventos y lugares")
    void escenario1_shouldRenderAdminPanelWithEventsAndVenues() throws Exception {
        // Dado que existen eventos y lugares registrados en el sistema
        venueRepository.save(new Venue(null, "Centro Puerta de Oro", "Via 40 # 79B", 3500));
        eventRepository.save(new Event(null, "DevFest Caribe 2026", LocalDate.of(2026, 11, 15), "Conferencia Tech"));

        // Cuando el coordinador accede al panel administrativo (/admin)
        mockMvc.perform(get("/admin"))
                // Entonces el sistema responde con estado 200 OK
                .andExpect(status().isOk())
                // Retorna la vista correcta "admin/index"
                .andExpect(view().name("admin/index"))
                // El modelo contiene las listas esperadas
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attributeExists("venues"))
                .andExpect(model().attribute("events", hasSize(1)))
                .andExpect(model().attribute("venues", hasSize(1)))
                // Renderiza los datos clave en el HTML
                .andExpect(content().string(containsString("DevFest Caribe 2026")))
                .andExpect(content().string(containsString("2026-11-15")))
                .andExpect(content().string(containsString("Conferencia Tech")))
                .andExpect(content().string(containsString("Centro Puerta de Oro")))
                .andExpect(content().string(containsString("Via 40 # 79B")))
                .andExpect(content().string(containsString("3500")));
    }

    @Test
    @DisplayName("Escenario 2: Gestión de Inventario Vacío (Caso de Borde) - Mensaje amigable cuando no hay eventos")
    void escenario2_shouldDisplayFriendlyMessageWhenCatalogIsEmpty() throws Exception {
        // Dado que no hay registros en la base de datos (setUp() ya limpió las tablas)

        // Cuando se consulta la vista de listado
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                // Entonces el sistema no debe mostrar una tabla vacía, sino el mensaje amigable exacto
                .andExpect(content().string(containsString("Actualmente no hay eventos programados")))
                .andExpect(content().string(containsString("Actualmente no hay lugares registrados")));
    }

    @Test
    @DisplayName("Escenario 3: Registro y Redirección Exitosa (Evento) - Guarda en BD y redirecciona al panel")
    void escenario3_shouldCreateEventAndRedirectToAdminPanel() throws Exception {
        // Dado un formulario de nuevo evento completado
        // Cuando el usuario hace clic en "Guardar" (POST a /admin/events)
        mockMvc.perform(post("/admin/events")
                        .param("nombre", "Seminario de Cloud Computing")
                        .param("fecha", "2026-12-10")
                        .param("descripcion", "Taller práctico sobre microservicios y despliegue en la nube"))
                // Entonces el sistema realiza una redirección automática al listado principal (Patrón PRG)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"));

        // Y confirma que el dato fue guardado en la base de datos
        var events = eventRepository.findAll();
        assertEquals(1, events.size());
        assertEquals("Seminario de Cloud Computing", events.get(0).getNombre());
        assertEquals(LocalDate.of(2026, 12, 10), events.get(0).getFecha());
    }

    @Test
    @DisplayName("Escenario 3 (Extendido): Registro y Redirección Exitosa (Venue) - Guarda en BD y redirecciona al panel")
    void escenario3_shouldCreateVenueAndRedirectToAdminPanel() throws Exception {
        // Dado un formulario de nuevo lugar completado
        // Cuando el usuario hace clic en "Guardar" (POST a /admin/venues)
        mockMvc.perform(post("/admin/venues")
                        .param("nombre", "Auditorio Central")
                        .param("direccion", "Carrera 45 # 80-12")
                        .param("capacidad", "1500"))
                // Entonces el sistema realiza una redirección automática al listado principal
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"));

        // Y confirma que el lugar fue guardado en la BD
        var venues = venueRepository.findAll();
        assertEquals(1, venues.size());
        assertEquals("Auditorio Central", venues.get(0).getNombre());
        assertEquals(1500, venues.get(0).getCapacidad());
    }

    @Test
    @DisplayName("Escenario 4: Validación de Navegación (Testing) - Model contiene la lista de eventos necesaria")
    void escenario4_shouldConfirmModelContainsEventsList() throws Exception {
        // Dado un test de integración de interfaz
        eventRepository.save(new Event(null, "Conferencia A", LocalDate.now(), "Desc"));

        // Cuando se invoca la ruta del panel
        mockMvc.perform(get("/admin"))
                // Entonces el test debe confirmar que el objeto Model contiene la lista de eventos necesaria
                .andExpect(status().isOk())
                .andExpect(view().name("admin/index"))
                .andExpect(model().attributeExists("events"))
                .andExpect(model().attribute("events", notNullValue()));
    }

    @Test
    @DisplayName("Formulario de Nuevo Evento: Debe renderizar la plantilla correcta y el objeto 'event' en el Model")
    void shouldRenderNewEventForm() throws Exception {
        mockMvc.perform(get("/admin/events/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/event-form"))
                .andExpect(model().attributeExists("event"))
                .andExpect(content().string(containsString("Registrar Nuevo Evento")))
                .andExpect(content().string(containsString("Guardar")));
    }

    @Test
    @DisplayName("Formulario de Nuevo Lugar: Debe renderizar la plantilla correcta y el objeto 'venue' en el Model")
    void shouldRenderNewVenueForm() throws Exception {
        mockMvc.perform(get("/admin/venues/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/venue-form"))
                .andExpect(model().attributeExists("venue"))
                .andExpect(content().string(containsString("Registrar Nuevo Lugar")))
                .andExpect(content().string(containsString("Guardar")));
    }

    @Test
    @DisplayName("Ruta Raíz (/): Redirecciona automáticamente al panel administrativo (/admin)")
    void shouldRedirectRootToAdmin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @DisplayName("TASK 3: Swagger OpenAPI configura grupo exclusivo para /api/** excluyendo /admin/**")
    void shouldConfigureSwaggerToOnlyIncludeApiPaths() {
        assertNotNull(apiGroup);
        assertEquals("eventify-api", apiGroup.getGroup());
        assertTrue(apiGroup.getPathsToMatch().contains("/api/**"));
        assertFalse(apiGroup.getPathsToMatch().contains("/admin/**"));
    }
}
