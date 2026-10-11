package com.eventify.repository;

import com.eventify.model.Venue;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class VenueRepository {
    private final List<Venue> venues = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Venue save(Venue venue){
        if (venue.getId()==null) {
            venue.setId(idGenerator.getAndIncrement());
        }
        venues.add(venue);
        return venue;
    }

    public List<Venue> findAll(){
        return new ArrayList<>(venues);
    }

    public Optional<Venue> findById(Long id) {
        return venues.stream()
                .filter(v -> v.getId().equals(id))
                .findFirst();
    }
}
