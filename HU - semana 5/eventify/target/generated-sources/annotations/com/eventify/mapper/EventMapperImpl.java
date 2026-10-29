package com.eventify.mapper;

import com.eventify.dto.EventCreateDTO;
import com.eventify.dto.EventResponseDTO;
import com.eventify.dto.EventUpdateDTO;
import com.eventify.model.Event;
import com.eventify.model.Venue;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-29T00:11:51-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.12.1 (Ubuntu)"
)
@Component
public class EventMapperImpl implements EventMapper {

    @Override
    public Event toEntity(EventCreateDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Event event = new Event();

        event.setNombre( dto.getNombre() );
        event.setFecha( dto.getFecha() );
        event.setDescripcion( dto.getDescripcion() );

        event.setActive( true );

        return event;
    }

    @Override
    public EventResponseDTO toResponseDTO(Event event) {
        if ( event == null ) {
            return null;
        }

        EventResponseDTO.EventResponseDTOBuilder eventResponseDTO = EventResponseDTO.builder();

        eventResponseDTO.venueName( eventVenueNombre( event ) );
        eventResponseDTO.venueCity( eventVenueCity( event ) );
        eventResponseDTO.categoryNames( mapCategoriesToNames( event.getCategories() ) );
        eventResponseDTO.id( event.getId() );
        eventResponseDTO.nombre( event.getNombre() );
        eventResponseDTO.fecha( event.getFecha() );
        eventResponseDTO.descripcion( event.getDescripcion() );
        eventResponseDTO.active( event.isActive() );

        return eventResponseDTO.build();
    }

    @Override
    public void updateEntityFromDTO(EventUpdateDTO dto, Event event) {
        if ( dto == null ) {
            return;
        }

        event.setNombre( dto.getNombre() );
        event.setFecha( dto.getFecha() );
        event.setDescripcion( dto.getDescripcion() );
    }

    private String eventVenueNombre(Event event) {
        Venue venue = event.getVenue();
        if ( venue == null ) {
            return null;
        }
        return venue.getNombre();
    }

    private String eventVenueCity(Event event) {
        Venue venue = event.getVenue();
        if ( venue == null ) {
            return null;
        }
        return venue.getCity();
    }
}
