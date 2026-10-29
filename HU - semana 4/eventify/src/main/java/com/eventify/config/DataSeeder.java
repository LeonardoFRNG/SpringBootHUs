package com.eventify.config;

import com.eventify.model.Category;
import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.service.CategoryService;
import com.eventify.service.EventService;
import com.eventify.service.VenueService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Set;

@Configuration
public class DataSeeder {

    @Bean
    @ConditionalOnProperty(name = "eventify.seeder.enabled", havingValue = "true", matchIfMissing = false)
    public CommandLineRunner initData(EventService eventService, VenueService venueService, CategoryService categoryService) {
        return args -> {
            // Seed de Lugares / Venues
            Venue venue1 = new Venue(null, "Centro de Convenciones Puerta de Oro", "Vía 40 # 79B-06", 5000, "Barranquilla");
            Venue venue2 = new Venue(null, "Teatro José Consuegra Higgins", "Calle 58 # 54-59", 800, "Barranquilla");
            venueService.create(venue1);
            venueService.create(venue2);

            // Categorías
            Category catConcerts = categoryService.getAll().stream()
                    .filter(c -> c.getName().equalsIgnoreCase("Concerts"))
                    .findFirst()
                    .orElseGet(() -> categoryService.create(new Category("Concerts", "Conciertos")));

            Category catConferences = categoryService.getAll().stream()
                    .filter(c -> c.getName().equalsIgnoreCase("Conferences"))
                    .findFirst()
                    .orElseGet(() -> categoryService.create(new Category("Conferences", "Conferencias")));

            // Seed de Eventos vinculados a sedes y categorías
            Event event1 = new Event(null, "DevFest Caribe 2026", LocalDate.of(2026, 11, 15),
                    "Conferencia de desarrollo de software y tecnología", venue1, Set.of(catConferences));
            Event event2 = new Event(null, "Festival de Música del Caribe", LocalDate.of(2026, 12, 5),
                    "Conciertos y presentaciones en vivo", venue2, Set.of(catConcerts));

            eventService.create(event1);
            eventService.create(event2);
        };
    }
}
