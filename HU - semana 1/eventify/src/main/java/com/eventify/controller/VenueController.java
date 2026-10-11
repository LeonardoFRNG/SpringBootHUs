package com.eventify.controller;

import com.eventify.model.Venue;
import com.eventify.service.VenueService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Venue create(@RequestBody Venue venue) {
        return venueService.create(venue);
    }

    @GetMapping
    public List<Venue> getAll() {
        return venueService.getAll();
    }
}