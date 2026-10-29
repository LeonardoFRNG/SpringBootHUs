package com.eventify.repository;

import com.eventify.dto.EventSummaryDTO;
import com.eventify.model.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // =========================================================================
    // Métodos heredados de la Semana 3 (Compatibilidad garantizada)
    // =========================================================================
    List<Event> findByNombreContainingIgnoreCase(String nombre);

    Page<Event> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // =========================================================================
    // Métodos avanzados HU - Semana 4
    // =========================================================================

    /**
     * Consulta paginada optimizada mediante Slice y proyección directa DTO.
     * Evita SELECT count(*) costosos y previene la carga masiva de entidades en memoria.
     * Soporta filtros combinables, búsquedas parciales e insensibles a mayúsculas/minúsculas.
     * Excluye automáticamente eventos inactivos gracias a @SQLRestriction("active = true").
     */
    @Query("""
        SELECT DISTINCT NEW com.eventify.dto.EventSummaryDTO(
            e.id,
            e.nombre,
            e.fecha,
            e.descripcion,
            v.nombre,
            v.city,
            v.capacidad
        )
        FROM Event e
        JOIN e.venue v
        LEFT JOIN e.categories c
        WHERE (:name IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:city IS NULL OR LOWER(v.city) LIKE LOWER(CONCAT('%', :city, '%')))
          AND (:category IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :category, '%')))
          AND (:minCapacity IS NULL OR v.capacidad >= :minCapacity)
          AND (:startDate IS NULL OR e.fecha >= :startDate)
          AND (:endDate IS NULL OR e.fecha <= :endDate)
        ORDER BY e.fecha DESC, e.id DESC
    """)
    Slice<EventSummaryDTO> findSummarySlice(
            @Param("name") String name,
            @Param("city") String city,
            @Param("category") String category,
            @Param("minCapacity") Integer minCapacity,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Consulta por lotes para cargar las categorías de una página de eventos en una única sentencia,
     * eliminando el problema N+1 sin acoplar un JOIN FETCH a una consulta paginada con colecciones ManyToMany.
     */
    @Query("""
        SELECT e.id, c.name
        FROM Event e
        JOIN e.categories c
        WHERE e.id IN :eventIds
        ORDER BY c.name ASC
    """)
    List<Object[]> findCategoryNamesByEventIds(@Param("eventIds") List<Long> eventIds);

    /**
     * Recupera un evento con sus relaciones Venue y Categories inicializadas via EntityGraph.
     */
    @EntityGraph(attributePaths = {"venue", "categories"})
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithRelations(@Param("id") Long id);

    /**
     * Consulta de auditoría exclusiva para eventos inactivos (borrado lógico).
     * Utiliza SQL nativo para omitir deliberadamente la restricción @SQLRestriction("active = true")
     * de Hibernate sin modificar ni eliminar registros físicamente.
     */
    @Query(value = """
        SELECT * FROM events
        WHERE active = FALSE
        ORDER BY fecha DESC, id DESC
    """, nativeQuery = true)
    List<Event> findInactiveEventsForAudit();
}
