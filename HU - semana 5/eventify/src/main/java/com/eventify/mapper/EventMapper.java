package com.eventify.mapper;

import com.eventify.dto.EventCreateDTO;
import com.eventify.dto.EventResponseDTO;
import com.eventify.dto.EventUpdateDTO;
import com.eventify.model.Category;
import com.eventify.model.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Mapper MapStruct para conversiones entre la entidad JPA Event y sus DTOs correspondientes.
 * Mantiene la responsabilidad de mapeo declarativo sin lógica de acceso a datos ni efectos colaterales.
 */
@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "venue", ignore = true) // Resuelto y validado en EventService
    @Mapping(target = "categories", ignore = true) // Resuelto y validado en EventService
    Event toEntity(EventCreateDTO dto);

    @Mapping(source = "venue.nombre", target = "venueName")
    @Mapping(source = "venue.city", target = "venueCity")
    @Mapping(source = "categories", target = "categoryNames", qualifiedByName = "mapCategoriesToNames")
    EventResponseDTO toResponseDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "categories", ignore = true)
    void updateEntityFromDTO(EventUpdateDTO dto, @MappingTarget Event event);

    @Named("mapCategoriesToNames")
    default List<String> mapCategoriesToNames(Set<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }
        return categories.stream()
                .map(Category::getName)
                .sorted()
                .toList();
    }
}
