package com.eventify.repository;

import com.eventify.model.Event;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class EventRepository {

    private final List<Event> events = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Event save(Event event) {
        if (event.getId()==null) {
            event.setId(idGenerator.getAndIncrement());
        }
        events.add(event);
        return event;
    }

    public List<Event> findAll() {
        return new ArrayList<>(events);
    }

    public Optional<Event> findById(Long id) {
        return events.stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();
    }
}
