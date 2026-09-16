package com.eventify.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //es una anotacion combo que genera en tiempos de compilacion getters, setters etc
@Builder //Implementa el patrón de diseño creacional Builder. Te permite instanciar objetos de forma legible sin depender de constructores gigantes con muchos parámetros
@NoArgsConstructor //genera un constructor vacio (sin parametros)
@AllArgsConstructor // genera un constructor con todos los atributos de la clase Venue en este caso
public class Venue {
    private Long id;
    private String name;
    private String address;
    private Integer capacity;
}
