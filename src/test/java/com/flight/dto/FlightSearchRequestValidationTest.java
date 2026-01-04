package com.flight.dto;

import com.flight.validation.DateRangeValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlightSearchRequestValidationTest {

    private DateRangeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new DateRangeValidator();
        context = mock(ConstraintValidatorContext.class);
        ConstraintValidatorContext.ConstraintViolationBuilder builder = 
            mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);
    }

    @Test
    void testValidDateRange() {
        FlightSearchRequest request = new FlightSearchRequest();
        request.setDepartureDate(LocalDate.now().plusDays(1));
        request.setReturnDate(LocalDate.now().plusDays(8));

        assertTrue(validator.isValid(request, context));
    }

    @Test
    void testInvalidDepartureDateInPast() {
        FlightSearchRequest request = new FlightSearchRequest();
        request.setDepartureDate(LocalDate.now().minusDays(1)); // Past date

        assertFalse(validator.isValid(request, context));
    }

    @Test
    void testInvalidReturnDateBeforeDeparture() {
        FlightSearchRequest request = new FlightSearchRequest();
        LocalDate departure = LocalDate.now().plusDays(10);
        request.setDepartureDate(departure);
        request.setReturnDate(departure.minusDays(1)); // Return before departure

        assertFalse(validator.isValid(request, context));
    }

    @Test
    void testValidOneWayTrip() {
        FlightSearchRequest request = new FlightSearchRequest();
        request.setDepartureDate(LocalDate.now().plusDays(1));
        // No return date

        assertTrue(validator.isValid(request, context));
    }

    @Test
    void testNullRequest() {
        assertTrue(validator.isValid(null, context)); // Let @NotNull handle null
    }
}

