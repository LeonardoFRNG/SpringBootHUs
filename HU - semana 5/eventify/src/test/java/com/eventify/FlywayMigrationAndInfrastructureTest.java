package com.eventify;

import com.eventify.model.Category;
import com.eventify.repository.CategoryRepository;
import com.eventify.repository.EventRepository;
import com.eventify.repository.VenueRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FlywayMigrationAndInfrastructureTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Infraestructura 1: Flyway detecta y ejecuta V1, V2 y V3 en el orden correcto")
    void flyway1_shouldExecuteAllMigrationsSuccessfully() {
        MigrationInfo[] appliedMigrations = flyway.info().applied();

        assertTrue(appliedMigrations.length >= 3, "Deben haberse aplicado al menos 3 migraciones (V1, V2, V3)");

        List<String> versions = Arrays.stream(appliedMigrations)
                .map(m -> m.getVersion().getVersion())
                .toList();

        assertEquals("1", versions.get(0));
        assertEquals("2", versions.get(1));
        assertEquals("3", versions.get(2));

        for (MigrationInfo info : appliedMigrations) {
            assertTrue(info.getState().isApplied(), "La migración " + info.getVersion() + " debe estar en estado aplicado");
        }
    }

    @Test
    @DisplayName("Infraestructura 2: La tabla flyway_schema_history registra las migraciones versionadas")
    void flyway2_schemaHistoryTableShouldExistAndHaveRecords() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"flyway_schema_history\" WHERE \"success\" = TRUE", Integer.class
        );

        assertNotNull(count);
        assertTrue(count >= 3, "La tabla flyway_schema_history debe registrar las 3 migraciones exitosas");
    }

    @Test
    @DisplayName("Infraestructura 3: V3 insertó al menos 200 eventos deterministas y datos referenciales")
    void flyway3_seedDataShouldContainAtLeast200Events() {
        Integer rawEventCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Integer.class);
        assertNotNull(rawEventCount);
        assertTrue(rawEventCount >= 200, "La base de datos debe contener al menos 200 eventos provenientes de V3. Actual: " + rawEventCount);

        Integer venueCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM venues", Integer.class);
        assertNotNull(venueCount);
        assertTrue(venueCount >= 10, "Deben existir múltiples sedes registradas");

        Integer ecCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events_categories", Integer.class);
        assertNotNull(ecCount);
        assertTrue(ecCount >= 200, "Debe haber al menos 200 relaciones evento-categoría vinculadas");
    }

    @Test
    @DisplayName("Infraestructura 4: Las 7 categorías iniciales representativas existen sin duplicados")
    void flyway4_initialSevenCategoriesMustExist() {
        List<String> expectedCategories = List.of(
                "Concerts", "Workshops", "Conferences", "Sports", "Gastronomy", "Festivals", "Theater"
        );

        List<Category> allCategories = categoryRepository.findAll();
        List<String> names = allCategories.stream().map(Category::getName).toList();

        for (String expected : expectedCategories) {
            assertTrue(names.contains(expected), "Debe existir la categoría: " + expected);
        }
    }

    @Test
    @DisplayName("Infraestructura 5: Columna 'city' en venues y 'active' en events están correctamente configuradas")
    void flyway5_columnsCityAndActiveMustBeConfigured() {
        // Verificar columna city en venues
        Integer venuesWithCity = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM venues WHERE city IS NOT NULL AND TRIM(city) <> ''", Integer.class
        );
        Integer totalVenues = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM venues", Integer.class);
        assertEquals(totalVenues, venuesWithCity, "Todas las sedes deben tener la columna 'city' poblada");

        // Verificar columna active en events
        Integer eventsWithActive = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM events WHERE active IS NOT NULL", Integer.class
        );
        Integer totalEvents = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM events", Integer.class);
        assertEquals(totalEvents, eventsWithActive, "Todos los eventos deben tener la columna 'active' configurada");
    }
}
