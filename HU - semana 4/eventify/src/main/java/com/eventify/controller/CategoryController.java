package com.eventify.controller;

import com.eventify.model.Category;
import com.eventify.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Categorías", description = "API REST para consulta y gestión del catálogo de categorías de eventos")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Listar todas las categorías disponibles")
    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAll();
    }

    @Operation(summary = "Obtener una categoría por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoría encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada")
    })
    @GetMapping("/{id}")
    public Category getById(@Parameter(description = "ID de la categoría", example = "1") @PathVariable Long id) {
        return categoryService.getById(id);
    }

    @Operation(summary = "Crear una nueva categoría")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Category create(@Valid @RequestBody Category category) {
        return categoryService.create(category);
    }
}
