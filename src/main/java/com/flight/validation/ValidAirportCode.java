package com.flight.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = AirportCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAirportCode {
    String message() default "Invalid airport IATA code. Valid codes: LAX, JFK, LHR, CDG, DXB, SIN, ORD, SFO";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

