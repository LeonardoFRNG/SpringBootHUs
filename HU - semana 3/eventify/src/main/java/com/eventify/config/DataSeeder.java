package com.eventify.config;

import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.service.EventService;
import com.eventify.service.VenueService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    @ConditionalOnProperty(name = "eventify.seeder.enabled", havingValue = "true", matchIfMissing = true)
    public CommandLineRunner initData(EventService eventService, VenueService venueService) {
        return args -> {
            // Seed de Lugares / Venues
            Venue venue1 = new Venue(null, "Centro de Convenciones Puerta de Oro", "Vía 40 # 79B-06", 5000);
            Venue venue2 = new Venue(null, "Teatro José Consuegra Higgins", "Calle 58 # 54-59", 800);
            venueService.create(venue1);
            venueService.create(venue2);

            // Seed de Eventos
            Event event1 = new Event(null, "DevFest Caribe 2026", LocalDate.of(2026, 11, 15), "Conferencia de desarrollo de software y tecnología");
            Event event2 = new Event(null, "Festival de Música del Caribe", LocalDate.of(2026, 12, 5), "Conciertos y presentaciones en vivo");
            eventService.create(event1);
            eventService.create(event2);
        };
    }
}