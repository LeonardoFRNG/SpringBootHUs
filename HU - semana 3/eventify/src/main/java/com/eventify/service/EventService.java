package com.eventify.service;

import com.eventify.exception.ResourceNotFoundException;
import com.eventify.model.Event;
import com.eventify.repository.EventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public Event create(Event event) {
        if (event.getNombre() == null || event.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        event.setId(null); // Asegura inserción nueva en BD
        return eventRepository.save(event);
    }

    public List<Event> getAll() {
        return eventRepository.findAll();
    }

    public Page<Event> getAll(Pageable pageable) {
        return eventRepository.findAll(pageable);
    }

    public Event getById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado con id: " + id));
    }

    public Event update(Long id, Event updatedEvent) {
        Event existing = getById(id);
        if (updatedEvent.getNombre() == null || updatedEvent.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del evento es obligatorio");
        }
        existing.setNombre(updatedEvent.getNombre());
        existing.setFecha(updatedEvent.getFecha());
        existing.setDescripcion(updatedEvent.getDescripcion());
        return eventRepository.save(existing);
    }

    public void delete(Long id) {
        Event existing = getById(id);
        eventRepository.delete(existing);
    }

    public Page<Event> searchByName(String nombre, Pageable pageable) {
        return eventRepository.findByNombreContainingIgnoreCase(nombre, pageable);
    }
}
