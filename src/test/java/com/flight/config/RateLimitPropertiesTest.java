package com.flight.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitPropertiesTest {

    private RateLimitProperties properties;

    @BeforeEach
    void setUp() {
        properties = new RateLimitProperties();
    }

    @Test
    void testDefaultValues() {
        assertEquals(10, properties.getCapacity());
        assertEquals(10, properties.getRefillTokens());
        assertEquals(60, properties.getRefillDuration());
        assertEquals("SECONDS", properties.getRefillDurationUnit());
    }

    @Test
    void testValidationWithValidValues() {
        properties.setCapacity(20);
        properties.setRefillTokens(20);
        properties.setRefillDuration(120);
        properties.setRefillDurationUnit("SECONDS");

        // Should not throw exception
        assertDoesNotThrow(() -> properties.validate());
    }

    @Test
    void testValidationFailsWithZeroCapacity() {
        properties.setCapacity(0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> properties.validate()
        );
        assertTrue(exception.getMessage().contains("capacity must be greater than 0"));
    }

    @Test
    void testValidationFailsWithNegativeCapacity() {
        properties.setCapacity(-1);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> properties.validate()
        );
        assertTrue(exception.getMessage().contains("capacity must be greater than 0"));
    }

    @Test
    void testValidationFailsWithZeroRefillTokens() {
        properties.setRefillTokens(0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> properties.validate()
        );
        assertTrue(exception.getMessage().contains("refill-tokens must be greater than 0"));
    }

    @Test
    void testValidationFailsWithZeroRefillDuration() {
        properties.setRefillDuration(0);

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> properties.validate()
        );
        assertTrue(exception.getMessage().contains("refill-duration must be greater than 0"));
    }

    @Test
    void testValidationFailsWithInvalidDurationUnit() {
        properties.setRefillDurationUnit("INVALID");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> properties.validate()
        );
        assertTrue(exception.getMessage().contains("refill-duration-unit must be one of"));
    }

    @Test
    void testValidationAcceptsValidDurationUnits() {
        String[] validUnits = {"SECONDS", "MINUTES", "HOURS", "DAYS"};
        
        for (String unit : validUnits) {
            properties.setRefillDurationUnit(unit);
            assertDoesNotThrow(() -> properties.validate());
        }
    }
}

