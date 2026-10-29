package com.eventify.exception;

/**
 * Excepción de dominio lanzada cuando se intenta crear o actualizar un recurso
 * con identificadores, nombres o combinaciones únicas que ya existen en el sistema.
 * Produce típicamente un estado HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
