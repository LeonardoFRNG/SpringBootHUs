package com.eventify.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Anotación de validación personalizada que rechaza fechas anteriores al día de hoy.
 * Permite eventos programados para el día actual y fechas futuras.
 */
@Documented
@Constraint(validatedBy = NoPastEventsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoPastEvents {

    String message() default "La fecha del evento no puede ser anterior a hoy";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
