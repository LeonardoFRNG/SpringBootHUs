package com.eventify.exception;

/**
 * Excepción de dominio lanzada cuando una operación viola una regla del negocio,
 * como rangos de fechas incongruentes o manipulación incompatible con el estado del recurso.
 * Produce típicamente un estado HTTP 400 Bad Request.
 */
public class BusinessRuleViolationException extends IllegalArgumentException {

    public BusinessRuleViolationException(String message) {
        super(message);
    }
}
