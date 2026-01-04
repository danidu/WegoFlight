package com.flight.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AirportCodeValidatorTest {

    private AirportCodeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new AirportCodeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidAirportCodes() {
        String[] validCodes = {"LAX", "JFK", "LHR", "CDG", "DXB", "SIN", "ORD", "SFO"};
        
        for (String code : validCodes) {
            assertTrue(validator.isValid(code, context), "Code " + code + " should be valid");
        }
    }

    @Test
    void testInvalidAirportCodes() {
        String[] invalidCodes = {"XXX", "ABC", "123", "INVALID", "ZZZ"};
        
        for (String code : invalidCodes) {
            assertFalse(validator.isValid(code, context), "Code " + code + " should be invalid");
        }
    }

    @Test
    void testCaseInsensitive() {
        // Valid codes should work case-insensitively
        assertTrue(validator.isValid("jfk", context), "jfk should be valid (case-insensitive)");
        assertTrue(validator.isValid("JFK", context), "JFK should be valid");
        assertTrue(validator.isValid("lax", context), "lax should be valid (case-insensitive)");
        assertTrue(validator.isValid("LAX", context), "LAX should be valid");
    }

    @Test
    void testNullValue() {
        assertTrue(validator.isValid(null, context)); // Let @NotBlank handle null
    }

    @Test
    void testEmptyString() {
        assertTrue(validator.isValid("", context)); // Let @NotBlank handle empty
    }
}

