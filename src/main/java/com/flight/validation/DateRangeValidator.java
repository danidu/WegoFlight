package com.flight.validation;

import com.flight.dto.FlightSearchRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, FlightSearchRequest> {
    
    @Override
    public void initialize(ValidDateRange constraintAnnotation) {
        // No initialization needed
    }
    
    @Override
    public boolean isValid(FlightSearchRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // Let @NotNull handle null validation
        }
        
        LocalDate today = LocalDate.now();
        LocalDate departureDate = request.getDepartureDate();
        LocalDate returnDate = request.getReturnDate();
        
        // Departure date must be in the future
        if (departureDate != null && !departureDate.isAfter(today)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Departure date must be in the future"
            ).addConstraintViolation();
            return false;
        }
        
        // If return date is provided, it must be after departure date
        if (returnDate != null && departureDate != null) {
            if (!returnDate.isAfter(departureDate)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Return date must be after departure date"
                ).addConstraintViolation();
                return false;
            }
        }
        
        return true;
    }
}

