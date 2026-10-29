package com.eventify.mapper;

import com.eventify.dto.VenueCreateDTO;
import com.eventify.dto.VenueResponseDTO;
import com.eventify.dto.VenueUpdateDTO;
import com.eventify.model.Venue;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-29T00:11:52-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Ubuntu)"
)
@Component
public class VenueMapperImpl implements VenueMapper {

    @Override
    public Venue toEntity(VenueCreateDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Venue venue = new Venue();

        venue.setCiudad( dto.getCiudad() );
        venue.setNombre( dto.getNombre() );
        venue.setDireccion( dto.getDireccion() );
        venue.setCapacidad( dto.getCapacidad() );
        venue.setCity( dto.getCity() );

        return venue;
    }

    @Override
    public VenueResponseDTO toResponseDTO(Venue venue) {
        if ( venue == null ) {
            return null;
        }

        VenueResponseDTO.VenueResponseDTOBuilder venueResponseDTO = VenueResponseDTO.builder();

        venueResponseDTO.id( venue.getId() );
        venueResponseDTO.nombre( venue.getNombre() );
        venueResponseDTO.direccion( venue.getDireccion() );
        venueResponseDTO.capacidad( venue.getCapacidad() );
        venueResponseDTO.city( venue.getCity() );

        return venueResponseDTO.build();
    }

    @Override
    public void updateEntityFromDTO(VenueUpdateDTO dto, Venue venue) {
        if ( dto == null ) {
            return;
        }

        venue.setCiudad( dto.getCiudad() );
        venue.setNombre( dto.getNombre() );
        venue.setDireccion( dto.getDireccion() );
        venue.setCapacidad( dto.getCapacidad() );
        venue.setCity( dto.getCity() );
    }
}
