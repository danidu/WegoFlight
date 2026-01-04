package com.flight.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AirlineCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAirlineCode {
    String message() default "Invalid airline IATA code. Valid codes: AA, UA, DL, BA, LH, AF, EK, SQ";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

