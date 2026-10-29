package com.eventify.controller;

import com.eventify.dto.VenueCreateDTO;
import com.eventify.dto.VenueResponseDTO;
import com.eventify.dto.VenueUpdateDTO;
import com.eventify.dto.validation.OnCreate;
import com.eventify.dto.validation.OnUpdate;
import com.eventify.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Lugares y Sedes (Venues)", description = "API REST para gestión de sedes, aforos y ciudades desacoplada mediante DTOs")
@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @Operation(summary = "Registrar un nuevo lugar o sede", description = "Crea un espacio físico validando duplicados de nombre y aplicando validación OnCreate.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lugar registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de validación inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Nombre de sede duplicado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<VenueResponseDTO> create(@Validated(OnCreate.class) @RequestBody VenueCreateDTO dto) {
        VenueResponseDTO created = venueService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Listar todas las sedes con paginación")
    @GetMapping
    public Page<VenueResponseDTO> getAll(@PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return venueService.getAllResponses(pageable);
    }

    @Operation(summary = "Listar todas las sedes sin paginación (para selectores)")
    @GetMapping("/all")
    public List<VenueResponseDTO> getAllList() {
        return venueService.getAllResponses();
    }

    @Operation(summary = "Obtener detalle de una sede por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sede encontrada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Sede no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public VenueResponseDTO getById(@Parameter(description = "ID del venue", example = "1") @PathVariable Long id) {
        return venueService.getByIdResponse(id);
    }

    @Operation(summary = "Actualizar información de una sede existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sede actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Sede no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Nombre duplicado con otra sede existente",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/{id}")
    public VenueResponseDTO update(
            @Parameter(description = "ID del venue a actualizar", example = "1") @PathVariable Long id,
            @Validated(OnUpdate.class) @RequestBody VenueUpdateDTO dto
    ) {
        if (dto.getId() == null) {
            dto.setId(id);
        }
        return venueService.update(id, dto);
    }

    @Operation(summary = "Eliminar físicamente una sede")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Sede eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Sede no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        venueService.delete(id);
    }

    @Operation(summary = "Buscar sedes por ciudad")
    @GetMapping("/search")
    public List<VenueResponseDTO> searchByCity(@Parameter(description = "Nombre o fragmento de la ciudad", example = "bog") @RequestParam String city) {
        return venueService.searchByCityResponses(city);
    }
}
