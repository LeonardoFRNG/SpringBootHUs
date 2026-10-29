package com.eventify.mapper;

import com.eventify.dto.*;
import com.eventify.model.Category;
import com.eventify.model.Event;
import com.eventify.model.Venue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MapStructAndDtoTest {

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private VenueMapper venueMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Test
    @DisplayName("MapStruct EventMapper: Debe mapear EventCreateDTO a Entity ignorando ID y activándolo por defecto")
    void shouldMapEventCreateDTOToEntity() {
        EventCreateDTO dto = EventCreateDTO.builder()
                .nombre("Spring I/O 2026")
                .fecha(LocalDate.now().plusMonths(3))
                .descripcion("Conferencia de Spring Boot y arquitectura moderna")
                .venueId(10L)
                .categoryIds(Set.of(1L, 2L))
                .build();

        Event entity = eventMapper.toEntity(dto);

        assertNotNull(entity);
        assertNull(entity.getId(), "El ID debe ser ignorado y quedar nulo para generación JPA");
        assertEquals("Spring I/O 2026", entity.getNombre());
        assertEquals(dto.getFecha(), entity.getFecha());
        assertEquals("Conferencia de Spring Boot y arquitectura moderna", entity.getDescripcion());
        assertTrue(entity.isActive(), "El evento mapeado debe tener active = true por defecto");
    }

    @Test
    @DisplayName("MapStruct EventMapper: Debe mapear Entity a EventResponseDTO incluyendo nombres de categorías y datos de Venue")
    void shouldMapEntityToEventResponseDTO() {
        Venue venue = new Venue(5L, "Movistar Arena", "Diagonal 61C", 14000, "Bogotá");
        Category cat1 = new Category(1L, "Conferences", "Charlas");
        Category cat2 = new Category(2L, "Workshops", "Talleres");

        Event event = new Event(42L, "DevFest Colombia", LocalDate.of(2026, 11, 20),
                "Gran evento de tecnología", venue, Set.of(cat1, cat2));
        event.setActive(true);

        EventResponseDTO response = eventMapper.toResponseDTO(event);

        assertNotNull(response);
        assertEquals(42L, response.getId());
        assertEquals("DevFest Colombia", response.getNombre());
        assertEquals(LocalDate.of(2026, 11, 20), response.getFecha());
        assertTrue(response.isActive());
        assertEquals("Movistar Arena", response.getVenueName());
        assertEquals("Bogotá", response.getVenueCity());
        assertNotNull(response.getCategoryNames());
        assertEquals(2, response.getCategoryNames().size());
        assertTrue(response.getCategoryNames().contains("Conferences"));
        assertTrue(response.getCategoryNames().contains("Workshops"));
    }

    @Test
    @DisplayName("MapStruct EventMapper: Debe actualizar entidad existente con EventUpdateDTO respetando MappingTarget")
    void shouldUpdateEntityFromEventUpdateDTO() {
        Venue venue = new Venue(1L, "Sede Original", "Cra 1", 1000, "Medellín");
        Event existing = new Event(15L, "Nombre Anterior", LocalDate.now().plusDays(5), "Desc Anterior", venue);

        EventUpdateDTO updateDTO = EventUpdateDTO.builder()
                .id(15L)
                .nombre("Nombre Renovado")
                .fecha(LocalDate.now().plusDays(10))
                .descripcion("Nueva Descripción")
                .venueId(2L)
                .build();

        eventMapper.updateEntityFromDTO(updateDTO, existing);

        assertEquals(15L, existing.getId(), "El ID existente no debe ser alterado");
        assertEquals("Nombre Renovado", existing.getNombre());
        assertEquals(LocalDate.now().plusDays(10), existing.getFecha());
        assertEquals("Nueva Descripción", existing.getDescripcion());
    }

    @Test
    @DisplayName("MapStruct VenueMapper: Debe mapear conversiones bidireccionales y actualización")
    void shouldMapVenueCreateAndResponseDTO() {
        VenueCreateDTO createDTO = VenueCreateDTO.builder()
                .nombre("Centro de Convenciones Cartagena")
                .direccion("Getsemaní Calle 24")
                .capacidad(4000)
                .city("Cartagena")
                .build();

        Venue entity = venueMapper.toEntity(createDTO);
        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals("Centro de Convenciones Cartagena", entity.getNombre());
        assertEquals("Cartagena", entity.getCity());

        entity.setId(100L);
        VenueResponseDTO responseDTO = venueMapper.toResponseDTO(entity);
        assertNotNull(responseDTO);
        assertEquals(100L, responseDTO.getId());
        assertEquals("Centro de Convenciones Cartagena", responseDTO.getNombre());
        assertEquals("Cartagena", responseDTO.getCity());

        VenueUpdateDTO updateDTO = VenueUpdateDTO.builder()
                .id(100L)
                .nombre("Centro de Convenciones Internacional")
                .direccion("Getsemaní Calle 24")
                .capacidad(5000)
                .city("Cartagena")
                .build();

        venueMapper.updateEntityFromDTO(updateDTO, entity);
        assertEquals("Centro de Convenciones Internacional", entity.getNombre());
        assertEquals(5000, entity.getCapacidad());
    }

    @Test
    @DisplayName("MapStruct CategoryMapper: Debe mapear CategoryCreateDTO y CategoryResponseDTO")
    void shouldMapCategoryCreateAndResponseDTO() {
        CategoryCreateDTO createDTO = CategoryCreateDTO.builder()
                .name("Hackathon")
                .description("Competencias de programación intensiva")
                .build();

        Category entity = categoryMapper.toEntity(createDTO);
        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals("Hackathon", entity.getName());
        assertEquals("Competencias de programación intensiva", entity.getDescription());

        entity.setId(77L);
        CategoryResponseDTO responseDTO = categoryMapper.toResponseDTO(entity);
        assertNotNull(responseDTO);
        assertEquals(77L, responseDTO.getId());
        assertEquals("Hackathon", responseDTO.getName());
    }
}
