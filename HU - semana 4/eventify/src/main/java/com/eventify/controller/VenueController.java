package com.eventify.controller;

import com.eventify.model.Venue;
import com.eventify.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Lugares y Sedes (Venues)", description = "API REST para gestión de sedes, aforos y ciudades")
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @Operation(summary = "Registrar un nuevo lugar o sede", description = "Crea un espacio físico indicando nombre, dirección, capacidad y ciudad obligatoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lugar registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de validación inválidos")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Venue create(@Valid @RequestBody Venue venue) {
        return venueService.create(venue);
    }

    @Operation(summary = "Listar todas las sedes con paginación")
    @GetMapping
    public Page<Venue> getAll(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return venueService.getAll(pageable);
    }

    @Operation(summary = "Listar todas las sedes sin paginación (para selectores)")
    @GetMapping("/all")
    public List<Venue> getAllList() {
        return venueService.getAll();
    }

    @Operation(summary = "Obtener detalle de una sede por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sede encontrada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Sede no encontrada")
    })
    @GetMapping("/{id}")
    public Venue getById(@Parameter(description = "ID del venue", example = "1") @PathVariable Long id) {
        return venueService.getById(id);
    }

    @Operation(summary = "Actualizar información de una sede existente")
    @PutMapping("/{id}")
    public Venue update(@PathVariable Long id, @Valid @RequestBody Venue venue) {
        return venueService.update(id, venue);
    }

    @Operation(summary = "Eliminar físicamente una sede")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        venueService.delete(id);
    }

    @Operation(summary = "Buscar sedes por ciudad")
    @GetMapping("/search")
    public List<Venue> searchByCity(@Parameter(description = "Nombre o fragmento de la ciudad", example = "bog") @RequestParam String city) {
        return venueService.searchByCity(city);
    }
}
