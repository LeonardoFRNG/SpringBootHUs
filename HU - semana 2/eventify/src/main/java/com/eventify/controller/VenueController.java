package com.eventify.controller;

import com.eventify.model.Venue;
import com.eventify.service.VenueService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    public Page<Venue> getAll(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return venueService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public Venue getById(@PathVariable Long id) {
        return venueService.getById(id);
    }

    @PutMapping("/{id}")
    public Venue update(@PathVariable Long id, @RequestBody Venue venue) {
        return venueService.update(id, venue);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        venueService.delete(id);
    }
}