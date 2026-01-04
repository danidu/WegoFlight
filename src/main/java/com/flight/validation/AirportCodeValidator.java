package com.flight.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class AirportCodeValidator implements ConstraintValidator<ValidAirportCode, String> {
    
    private static final Set<String> VALID_AIRPORT_CODES = Set.of(
        "LAX", "JFK", "LHR", "CDG", "DXB", "SIN", "ORD", "SFO"
    );
    
    @Override
    public void initialize(ValidAirportCode constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true; // Let @NotBlank handle null/empty validation
        }
        return VALID_AIRPORT_CODES.contains(value.toUpperCase());
    }
}

