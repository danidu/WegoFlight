package com.flight.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Rate limit configuration properties with validation
 * 
 * Default values provide safe defaults for production use.
 * All values are validated at startup to prevent misconfiguration.
 */
@Component
@ConfigurationProperties(prefix = "rate-limit.flights.search")
public class RateLimitProperties {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitProperties.class);
    
    // Safe defaults
    private int capacity = 10;
    private int refillTokens = 10;
    private int refillDuration = 60;
    private String refillDurationUnit = "SECONDS";

    @PostConstruct
    public void validate() {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                "rate-limit.flights.search.capacity must be greater than 0. Current value: " + capacity
            );
        }
        
        if (refillTokens <= 0) {
            throw new IllegalArgumentException(
                "rate-limit.flights.search.refill-tokens must be greater than 0. Current value: " + refillTokens
            );
        }
        
        if (refillDuration <= 0) {
            throw new IllegalArgumentException(
                "rate-limit.flights.search.refill-duration must be greater than 0. Current value: " + refillDuration
            );
        }
        
        if (!StringUtils.hasText(refillDurationUnit)) {
            throw new IllegalArgumentException(
                "rate-limit.flights.search.refill-duration-unit cannot be empty"
            );
        }
        
        // Validate duration unit
        String upperUnit = refillDurationUnit.toUpperCase();
        if (!upperUnit.matches("SECONDS|MINUTES|HOURS|DAYS")) {
            throw new IllegalArgumentException(
                "rate-limit.flights.search.refill-duration-unit must be one of: SECONDS, MINUTES, HOURS, DAYS. " +
                "Current value: " + refillDurationUnit
            );
        }
        
        logger.info("Rate limit configuration validated - Capacity: {}, RefillTokens: {}, RefillDuration: {} {}, " +
                   "RefillDurationUnit: {}", capacity, refillTokens, refillDuration, refillDurationUnit);
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRefillTokens() {
        return refillTokens;
    }

    public void setRefillTokens(int refillTokens) {
        this.refillTokens = refillTokens;
    }

    public int getRefillDuration() {
        return refillDuration;
    }

    public void setRefillDuration(int refillDuration) {
        this.refillDuration = refillDuration;
    }

    public String getRefillDurationUnit() {
        return refillDurationUnit;
    }

    public void setRefillDurationUnit(String refillDurationUnit) {
        this.refillDurationUnit = refillDurationUnit;
    }
}

