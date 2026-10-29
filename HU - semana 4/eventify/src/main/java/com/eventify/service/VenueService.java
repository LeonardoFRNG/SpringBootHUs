package com.eventify.service;

import com.eventify.exception.ResourceNotFoundException;
import com.eventify.model.Venue;
import com.eventify.repository.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    public Venue create(Venue venue) {
        validateVenue(venue);
        venue.setId(null);
        return venueRepository.save(venue);
    }

    @Transactional(readOnly = true)
    public List<Venue> getAll() {
        return venueRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Page<Venue> getAll(Pageable pageable) {
        return venueRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Venue getById(Long id) {
        return venueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venue no encontrado con id: " + id));
    }

    public Venue update(Long id, Venue updatedVenue) {
        Venue existing = getById(id);
        validateVenue(updatedVenue);
        existing.setNombre(updatedVenue.getNombre());
        existing.setDireccion(updatedVenue.getDireccion());
        existing.setCapacidad(updatedVenue.getCapacidad());
        existing.setCity(updatedVenue.getCity());
        return venueRepository.save(existing);
    }

    public void delete(Long id) {
        Venue existing = getById(id);
        venueRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<Venue> searchByCity(String city) {
        return venueRepository.findByCityContainingIgnoreCase(city);
    }

    private void validateVenue(Venue venue) {
        if (venue.getNombre() == null || venue.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del venue es obligatorio");
        }
        if (venue.getCapacidad() == null || venue.getCapacidad() <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a 0");
        }
        if (venue.getCity() == null || venue.getCity().isBlank()) {
            throw new IllegalArgumentException("La ciudad del venue es obligatoria");
        }
    }
}
