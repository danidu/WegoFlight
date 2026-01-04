package com.flight.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rate-limit.flights.search")
public class RateLimitProperties {
    private int capacity = 10;
    private int refillTokens = 10;
    private int refillDuration = 60;
    private String refillDurationUnit = "SECONDS";

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

