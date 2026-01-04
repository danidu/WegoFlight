package com.flight.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AirlineCodeValidatorTest {

    private AirlineCodeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new AirlineCodeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidAirlineCodes() {
        String[] validCodes = {"AA", "UA", "DL", "BA", "LH", "AF", "EK", "SQ"};
        
        for (String code : validCodes) {
            assertTrue(validator.isValid(code, context), "Code " + code + " should be valid");
        }
    }

    @Test
    void testInvalidAirlineCodes() {
        String[] invalidCodes = {"XXX", "ABC", "123", "invalid"};
        
        for (String code : invalidCodes) {
            assertFalse(validator.isValid(code, context), "Code " + code + " should be invalid");
        }
    }

    @Test
    void testValidAirlineCodeList() {
        List<String> validList = Arrays.asList("EK", "SQ", "BA");
        assertTrue(validator.isValid(validList, context));
    }

    @Test
    void testInvalidAirlineCodeInList() {
        List<String> invalidList = Arrays.asList("EK", "XXX", "SQ");
        assertFalse(validator.isValid(invalidList, context));
    }

    @Test
    void testEmptyList() {
        List<String> emptyList = Arrays.asList();
        assertTrue(validator.isValid(emptyList, context)); // Empty list is valid
    }

    @Test
    void testNullValue() {
        assertTrue(validator.isValid(null, context)); // Let other validators handle null
    }
}

