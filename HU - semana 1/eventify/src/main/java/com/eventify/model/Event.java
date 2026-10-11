package com.eventify.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Long id;
    private String nombre;
    private LocalDate fecha;
    private String desccripcion;
}
