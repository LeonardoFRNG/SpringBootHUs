package com.eventify.Repository;

import com.eventify.model.Event;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EventRepository {
    private final List<Event> events = new ArrayList<>();
    private long nextId = 1L;

    public Event save(Event event) {
        event.setId(nextId++);
        events.add(event);
        return event;
    }

    public List<Event> findAll() {
        return events;
    }
}
