package com.eventify.service;

import com.eventify.dto.CategoryCreateDTO;
import com.eventify.dto.CategoryResponseDTO;
import com.eventify.exception.DuplicateResourceException;
import com.eventify.exception.ResourceNotFoundException;
import com.eventify.mapper.CategoryMapper;
import com.eventify.model.Category;
import com.eventify.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @org.springframework.beans.factory.annotation.Autowired
    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public CategoryService(CategoryRepository categoryRepository) {
        this(categoryRepository, null);
    }

    // =========================================================================
    // Operaciones con DTOs y MapStruct (HU - Semana 5)
    // =========================================================================

    public CategoryResponseDTO create(CategoryCreateDTO dto) {
        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + dto.getName());
        }
        Category category = categoryMapper.toEntity(dto);
        category.setId(null);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getByIdResponse(Long id) {
        return categoryMapper.toResponseDTO(getById(id));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllResponses() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(categoryMapper::toResponseDTO)
                .toList();
    }

    // =========================================================================
    // Métodos con entidades para compatibilidad MVC y pruebas previas
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Category> getAll() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Category getById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    }

    public Category create(Category category) {
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        }
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + category.getName());
        }
        category.setId(null);
        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Set<Category> getByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(categoryRepository.findAllById(ids));
    }
}
