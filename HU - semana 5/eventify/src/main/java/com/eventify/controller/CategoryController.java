package com.eventify.controller;

import com.eventify.dto.CategoryCreateDTO;
import com.eventify.dto.CategoryResponseDTO;
import com.eventify.dto.validation.OnCreate;
import com.eventify.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Categorías", description = "API REST para consulta y gestión del catálogo de categorías de eventos desacoplada mediante DTOs")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Listar todas las categorías disponibles")
    @GetMapping
    public List<CategoryResponseDTO> getAll() {
        return categoryService.getAllResponses();
    }

    @Operation(summary = "Obtener una categoría por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public CategoryResponseDTO getById(@Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
        return categoryService.getByIdResponse(id);
    }

    @Operation(summary = "Crear una nueva categoría")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "409", description = "Nombre de categoría duplicado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@Validated(OnCreate.class) @RequestBody CategoryCreateDTO dto) {
        CategoryResponseDTO created = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
