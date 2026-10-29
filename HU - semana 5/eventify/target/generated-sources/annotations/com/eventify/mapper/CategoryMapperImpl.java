package com.eventify.mapper;

import com.eventify.dto.CategoryCreateDTO;
import com.eventify.dto.CategoryResponseDTO;
import com.eventify.model.Category;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-29T00:11:52-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Ubuntu)"
)
@Component
public class CategoryMapperImpl implements CategoryMapper {

    @Override
    public Category toEntity(CategoryCreateDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Category category = new Category();

        category.setNombre( dto.getNombre() );
        category.setName( dto.getName() );
        category.setDescription( dto.getDescription() );

        return category;
    }

    @Override
    public CategoryResponseDTO toResponseDTO(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponseDTO.CategoryResponseDTOBuilder categoryResponseDTO = CategoryResponseDTO.builder();

        categoryResponseDTO.id( category.getId() );
        categoryResponseDTO.name( category.getName() );
        categoryResponseDTO.description( category.getDescription() );

        return categoryResponseDTO.build();
    }
}
