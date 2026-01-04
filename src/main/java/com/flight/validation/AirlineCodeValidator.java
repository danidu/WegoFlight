package com.flight.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Set;

public class AirlineCodeValidator implements ConstraintValidator<ValidAirlineCode, Object> {
    
    private static final Set<String> VALID_AIRLINE_CODES = Set.of(
        "AA", "UA", "DL", "BA", "LH", "AF", "EK", "SQ"
    );
    
    @Override
    public void initialize(ValidAirlineCode constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let other validators handle null
        }
        
        if (value instanceof String) {
            return VALID_AIRLINE_CODES.contains(((String) value).toUpperCase());
        }
        
        if (value instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> codes = (List<String>) value;
            if (codes.isEmpty()) {
                return true; // Empty list is valid
            }
            return codes.stream()
                .allMatch(code -> code != null && VALID_AIRLINE_CODES.contains(code.toUpperCase()));
        }
        
        return false;
    }
}

