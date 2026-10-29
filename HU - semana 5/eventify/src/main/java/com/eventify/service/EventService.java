package com.eventify.service;

import com.eventify.dto.EventCreateDTO;
import com.eventify.dto.EventResponseDTO;
import com.eventify.dto.EventSummaryDTO;
import com.eventify.dto.EventUpdateDTO;
import com.eventify.exception.BusinessRuleViolationException;
import com.eventify.exception.ResourceNotFoundException;
import com.eventify.mapper.EventMapper;
import com.eventify.model.Category;
import com.eventify.model.Event;
import com.eventify.model.Venue;
import com.eventify.repository.CategoryRepository;
import com.eventify.repository.EventRepository;
import com.eventify.repository.VenueRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public EventService(EventRepository eventRepository,
                        VenueRepository venueRepository,
                        CategoryRepository categoryRepository,
                        EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.categoryRepository = categoryRepository;
        this.eventMapper = eventMapper;
    }

    public EventService(EventRepository eventRepository, VenueRepository venueRepository) {
        this(eventRepository, venueRepository, null, null);
    }

    // =========================================================================
    // Operaciones con DTOs y MapStruct (HU - Semana 5)
    // =========================================================================

    /**
     * Crea un nuevo evento a partir del DTO de entrada.
     * Valida la existencia de la sede y de todas las categorías asociadas.
     * Asigna estado activo (borrado lógico por defecto) y retorna EventResponseDTO.
     */
    public EventResponseDTO create(EventCreateDTO dto) {
        Venue venue = venueRepository.findById(dto.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede (Venue) no encontrada con id: " + dto.getVenueId()));

        Set<Category> categories = new HashSet<>();
        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            List<Category> foundCategories = categoryRepository.findAllById(dto.getCategoryIds());
            if (foundCategories.size() != dto.getCategoryIds().size()) {
                Set<Long> foundIds = foundCategories.stream().map(Category::getId).collect(Collectors.toSet());
                List<Long> missingIds = dto.getCategoryIds().stream().filter(id -> !foundIds.contains(id)).toList();
                throw new ResourceNotFoundException("Categoría(s) no encontrada(s) con id(s): " + missingIds);
            }
            categories.addAll(foundCategories);
        }

        Event event = eventMapper.toEntity(dto);
        event.setId(null);
        event.setActive(true);
        event.setVenue(venue);
        event.setCategories(categories);

        Event saved = eventRepository.save(event);
        return eventMapper.toResponseDTO(saved);
    }

    /**
     * Actualiza un evento existente utilizando el DTO de entrada.
     * Recupera la entidad con sus relaciones, mapea los cambios y retorna EventResponseDTO.
     */
    public EventResponseDTO update(Long id, EventUpdateDTO dto) {
        Event existing = eventRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));

        if (dto.getVenueId() != null) {
            Venue venue = venueRepository.findById(dto.getVenueId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sede (Venue) no encontrada con id: " + dto.getVenueId()));
            existing.setVenue(venue);
        }

        if (dto.getCategoryIds() != null) {
            if (!dto.getCategoryIds().isEmpty()) {
                List<Category> foundCategories = categoryRepository.findAllById(dto.getCategoryIds());
                if (foundCategories.size() != dto.getCategoryIds().size()) {
                    Set<Long> foundIds = foundCategories.stream().map(Category::getId).collect(Collectors.toSet());
                    List<Long> missingIds = dto.getCategoryIds().stream().filter(catId -> !foundIds.contains(catId)).toList();
                    throw new ResourceNotFoundException("Categoría(s) no encontrada(s) con id(s): " + missingIds);
                }
                existing.setCategories(new HashSet<>(foundCategories));
            } else {
                existing.setCategories(new HashSet<>());
            }
        }

        eventMapper.updateEntityFromDTO(dto, existing);
        Event saved = eventRepository.save(existing);
        return eventMapper.toResponseDTO(saved);
    }

    /**
     * Consulta individual detallada de un evento por ID retornando EventResponseDTO.
     */
    @Transactional(readOnly = true)
    public EventResponseDTO getByIdResponse(Long id) {
        Event event = eventRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
        return eventMapper.toResponseDTO(event);
    }

    // =========================================================================
    // Métodos con entidades y consultas optimizadas heredadas de Semanas 3 y 4
    // =========================================================================

    public Event create(Event event) {
        validateEvent(event);
        event.setId(null);
        event.setActive(true);

        if (event.getVenue().getId() != null) {
            Venue attachedVenue = venueRepository.findById(event.getVenue().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue no encontrado con id: " + event.getVenue().getId()));
            event.setVenue(attachedVenue);
        }

        return eventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Event> getAll(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
    }

    @Transactional(readOnly = true)
    public Event getByIdWithRelations(Long id) {
        return eventRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
    }

    public Event update(Long id, Event updatedEvent) {
        Event existing = getByIdWithRelations(id);
        validateEvent(updatedEvent);

        existing.setNombre(updatedEvent.getNombre());
        existing.setFecha(updatedEvent.getFecha());
        existing.setDescripcion(updatedEvent.getDescripcion());

        if (updatedEvent.getVenue() != null && updatedEvent.getVenue().getId() != null) {
            Venue attachedVenue = venueRepository.findById(updatedEvent.getVenue().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Venue no encontrado con id: " + updatedEvent.getVenue().getId()));
            existing.setVenue(attachedVenue);
        }

        if (updatedEvent.getCategories() != null) {
            existing.setCategories(new HashSet<>(updatedEvent.getCategories()));
        }

        return eventRepository.save(existing);
    }

    /**
     * Borrado lógico: desactiva el evento (active = false).
     * No realiza DELETE físico ni borra lugares o categorías asociadas.
     */
    public void softDelete(Long id) {
        Event existing = getById(id);
        existing.softDelete();
        eventRepository.save(existing);
    }

    public void deactivate(Long id) {
        softDelete(id);
    }

    public void delete(Long id) {
        softDelete(id);
    }

    @Transactional(readOnly = true)
    public Page<Event> searchByName(String nombre, Pageable pageable) {
        return eventRepository.findByNombreContainingIgnoreCase(nombre, pageable);
    }

    /**
     * Búsqueda y paginación optimizada mediante Slice<EventSummaryDTO>.
     * Resuelve el problema N+1 mediante 2 consultas exactas:
     * 1. Consulta principal paginada con proyección JPQL constructor (SELECT NEW) y JOIN con Venue.
     * 2. Consulta por lotes para asociar categorías a los eventos de la página actual.
     */
    @Transactional(readOnly = true)
    public Slice<EventSummaryDTO> searchEventsSummary(
            String name,
            String city,
            String category,
            Integer minCapacity,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        // Validación de coherencia temporal mediante BusinessRuleViolationException
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessRuleViolationException("La fecha inicial (" + startDate + ") no puede ser posterior a la fecha final (" + endDate + ")");
        }

        String normName = normalizeFilter(name);
        String normCity = normalizeFilter(city);
        String normCategory = normalizeFilter(category);

        Slice<EventSummaryDTO> slice = eventRepository.findSummarySlice(
                normName,
                normCity,
                normCategory,
                minCapacity,
                startDate,
                endDate,
                pageable
        );

        List<EventSummaryDTO> content = slice.getContent();
        if (content.isEmpty()) {
            return slice;
        }

        List<Long> eventIds = content.stream().map(EventSummaryDTO::id).toList();
        List<Object[]> categoryRows = eventRepository.findCategoryNamesByEventIds(eventIds);

        Map<Long, List<String>> categoriesByEventId = categoryRows.stream()
                .collect(Collectors.groupingBy(
                        row -> (Long) row[0],
                        Collectors.mapping(row -> (String) row[1], Collectors.toList())
                ));

        List<EventSummaryDTO> enrichedContent = content.stream()
                .map(dto -> dto.withCategories(categoriesByEventId.getOrDefault(dto.id(), Collections.emptyList())))
                .toList();

        return new SliceImpl<>(enrichedContent, slice.getPageable(), slice.hasNext());
    }

    /**
     * Consulta de auditoría para consultar eventos inactivos.
     */
    @Transactional(readOnly = true)
    public List<Event> getInactiveEventsForAudit() {
        return eventRepository.findInactiveEventsForAudit();
    }

    @Transactional(readOnly = true)
    public List<EventResponseDTO> getInactiveEventsForAuditDTO() {
        return eventRepository.findInactiveEventsForAudit().stream()
                .map(eventMapper::toResponseDTO)
                .toList();
    }

    private void validateEvent(Event event) {
        if (event.getNombre() == null || event.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        if (event.getFecha() == null) {
            throw new IllegalArgumentException("La fecha del evento es obligatoria");
        }
        if (event.getVenue() == null) {
            throw new IllegalArgumentException("El evento debe estar asociado a una sede (venue)");
        }
    }

    private String normalizeFilter(String value) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : null;
    }
}
