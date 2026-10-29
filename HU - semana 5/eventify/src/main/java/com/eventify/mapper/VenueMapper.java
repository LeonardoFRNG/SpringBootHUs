package com.eventify.mapper;

import com.eventify.dto.VenueCreateDTO;
import com.eventify.dto.VenueResponseDTO;
import com.eventify.dto.VenueUpdateDTO;
import com.eventify.model.Venue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VenueMapper {

    @Mapping(target = "id", ignore = true)
    Venue toEntity(VenueCreateDTO dto);

    VenueResponseDTO toResponseDTO(Venue venue);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(VenueUpdateDTO dto, @MappingTarget Venue venue);
}
