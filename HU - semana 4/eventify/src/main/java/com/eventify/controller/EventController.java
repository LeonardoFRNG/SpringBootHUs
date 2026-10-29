package com.eventify.controller;

import com.eventify.dto.EventSummaryDTO;
import com.eventify.model.Event;
import com.eventify.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Eventos", description = "API REST para gestión, búsqueda y optimización del catálogo de eventos")
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(
            summary = "Listado y catálogo optimizado con filtros combinables y paginación Slice",
            description = """
                    Recupera un listado optimizado de eventos mediante la proyección Java Record `EventSummaryDTO`
                    y paginación `Slice`. Evita la consulta total COUNT(*), resuelve el problema N+1 y excluye
                    automáticamente eventos inactivos mediante `@SQLRestriction`.
                    Permite combinar filtros por nombre, ciudad, categoría, capacidad mínima y rango de fechas.
                    El ordenamiento por defecto es cronológico descendente (fecha DESC, id DESC).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slice de eventos obtenido exitosamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de búsqueda inválidos (ej. fecha inicial posterior a fecha final)")
    })
    @GetMapping
    public Slice<EventSummaryDTO> getAll(
            @Parameter(description = "Búsqueda parcial o completa por nombre del evento (insensible a mayúsculas/minúsculas)", example = "rock")
            @RequestParam(required = false) String search,

            @Parameter(description = "Filtro por nombre del evento (alias de search)", example = "DevFest")
            @RequestParam(required = false) String name,

            @Parameter(description = "Búsqueda parcial por ciudad de la sede (ej. 'bog' para 'Bogotá')", example = "bog")
            @RequestParam(required = false) String city,

            @Parameter(description = "Búsqueda parcial por categoría (ej. 'rock', 'Concerts', 'Workshops')", example = "Concerts")
            @RequestParam(required = false) String category,

            @Parameter(description = "Capacidad mínima requerida de la sede", example = "5000")
            @RequestParam(required = false) Integer minCapacity,

            @Parameter(description = "Fecha inicial de rango (formato ISO: YYYY-MM-DD)", example = "2026-01-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Fecha final de rango (formato ISO: YYYY-MM-DD)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @PageableDefault(size = 10) Pageable pageable
    ) {
        String filterName = (search != null && !search.isBlank()) ? search : name;
        return eventService.searchEventsSummary(filterName, city, category, minCapacity, startDate, endDate, pageable);
    }

    @Operation(summary = "Obtener detalle completo de un evento por ID con sus relaciones")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento encontrado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado o inactivo")
    })
    @GetMapping("/{id}")
    public Event getById(@Parameter(description = "Identificador único del evento", example = "1") @PathVariable Long id) {
        return eventService.getByIdWithRelations(id);
    }

    @Operation(summary = "Registrar un nuevo evento", description = "Crea un nuevo evento activo por defecto vinculado a una sede obligatoria y categorías opcionales.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Evento creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o sede no especificada")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Event create(@Valid @RequestBody Event event) {
        return eventService.create(event);
    }

    @Operation(summary = "Actualizar información de un evento existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Evento actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PutMapping("/{id}")
    public Event update(@PathVariable Long id, @Valid @RequestBody Event event) {
        return eventService.update(id, event);
    }

    @Operation(
            summary = "Borrado lógico (desactivación) de un evento",
            description = """
                    Ejecuta un borrado lógico estableciendo `active = false`. El registro físico
                    permanece en la base de datos para preservar la trazabilidad histórica y NO elimina
                    sedes ni categorías compartidas. Las consultas públicas y administrativas normales
                    lo excluyen automáticamente mediante Hibernate `@SQLRestriction`.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Evento desactivado lógicamente exitosamente"),
            @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@Parameter(description = "ID del evento a desactivar", example = "1") @PathVariable Long id) {
        eventService.softDelete(id);
    }

    @Operation(
            summary = "Auditoría de eventos inactivos",
            description = "Consulta administrativa especial mediante consulta nativa que recupera registros desactivados por borrado lógico."
    )
    @GetMapping("/audit/inactive")
    public List<Event> getInactiveEventsForAudit() {
        return eventService.getInactiveEventsForAudit();
    }
}
