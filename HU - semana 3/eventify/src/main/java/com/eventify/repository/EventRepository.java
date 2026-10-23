package com.eventify.repository;

import com.eventify.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Derived Query para búsqueda por coincidencia parcial insensible a mayúsculas
    List<Event> findByNombreContainingIgnoreCase(String nombre);

    // Derived Query con soporte directo de Paginación
    Page<Event> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}