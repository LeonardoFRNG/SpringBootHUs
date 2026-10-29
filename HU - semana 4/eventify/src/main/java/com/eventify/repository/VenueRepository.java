package com.eventify.repository;

import com.eventify.model.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {

    List<Venue> findByNombreContainingIgnoreCase(String nombre);

    Page<Venue> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    List<Venue> findByCityContainingIgnoreCase(String city);

    List<Venue> findAllByOrderByNombreAsc();
}
