package com.eventify.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

/**
 * Validador para la restricción @NoPastEvents.
 * 
 * Regla de negocio:
 * - Si el valor es null, retorna true para delegar la obligatoriedad a @NotNull.
 * - Si fecha < hoy (LocalDate.now()): RECHAZADO (retorna false).
 * - Si fecha == hoy: VÁLIDO (retorna true).
 * - Si fecha > hoy: VÁLIDO (retorna true).
 */
public class NoPastEventsValidator implements ConstraintValidator<NoPastEvents, LocalDate> {

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // La obligatoriedad la gestiona @NotNull
        }
        LocalDate today = LocalDate.now();
        return !date.isBefore(today);
    }
}
