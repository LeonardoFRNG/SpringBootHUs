package com.eventify.controller;

import com.eventify.model.Event;
import com.eventify.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Event create(@RequestBody Event event) {
        return eventService.create(event);
    }

    @GetMapping
    public List<Event> getAll() {
        return eventService.getAll();
    }
}