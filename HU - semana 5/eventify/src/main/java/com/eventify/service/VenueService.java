package com.eventify.service;

import com.eventify.dto.VenueCreateDTO;
import com.eventify.dto.VenueResponseDTO;
import com.eventify.dto.VenueUpdateDTO;
import com.eventify.exception.DuplicateResourceException;
import com.eventify.exception.ResourceNotFoundException;
import com.eventify.mapper.VenueMapper;
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
    private final VenueMapper venueMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public VenueService(VenueRepository venueRepository, VenueMapper venueMapper) {
        this.venueRepository = venueRepository;
        this.venueMapper = venueMapper;
    }

    public VenueService(VenueRepository venueRepository) {
        this(venueRepository, null);
    }

    // =========================================================================
    // Operaciones con DTOs y MapStruct (HU - Semana 5)
    // =========================================================================

    public VenueResponseDTO create(VenueCreateDTO dto) {
        if (venueRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new DuplicateResourceException("Ya existe una sede con el nombre: " + dto.getNombre());
        }
        Venue venue = venueMapper.toEntity(dto);
        venue.setId(null);
        Venue saved = venueRepository.save(venue);
        return venueMapper.toResponseDTO(saved);
    }

    public VenueResponseDTO update(Long id, VenueUpdateDTO dto) {
        Venue existing = getById(id);
        if (venueRepository.existsByNombreIgnoreCaseAndIdNot(dto.getNombre(), id)) {
            throw new DuplicateResourceException("Ya existe una sede con el nombre: " + dto.getNombre());
        }
        venueMapper.updateEntityFromDTO(dto, existing);
        Venue saved = venueRepository.save(existing);
        return venueMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public VenueResponseDTO getByIdResponse(Long id) {
        return venueMapper.toResponseDTO(getById(id));
    }

    @Transactional(readOnly = true)
    public List<VenueResponseDTO> getAllResponses() {
        return venueRepository.findAllByOrderByNombreAsc().stream()
                .map(venueMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<VenueResponseDTO> getAllResponses(Pageable pageable) {
        return venueRepository.findAll(pageable).map(venueMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<VenueResponseDTO> searchByCityResponses(String city) {
        return venueRepository.findByCityContainingIgnoreCase(city).stream()
                .map(venueMapper::toResponseDTO)
                .toList();
    }

    // =========================================================================
    // Métodos con entidades para compatibilidad MVC y pruebas previas
    // =========================================================================

    public Venue create(Venue venue) {
        validateVenue(venue);
        if (venue.getNombre() != null && venueRepository.existsByNombreIgnoreCase(venue.getNombre())) {
            throw new DuplicateResourceException("Ya existe una sede con el nombre: " + venue.getNombre());
        }
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
        if (venueRepository.existsByNombreIgnoreCaseAndIdNot(updatedVenue.getNombre(), id)) {
            throw new DuplicateResourceException("Ya existe una sede con el nombre: " + updatedVenue.getNombre());
        }
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
