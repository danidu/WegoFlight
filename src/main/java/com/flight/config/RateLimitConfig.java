package com.flight.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    private final RateLimitProperties rateLimitProperties;

    public RateLimitConfig(RateLimitProperties rateLimitProperties) {
        this.rateLimitProperties = rateLimitProperties;
    }

    @Bean
    public Bucket searchEndpointBucket() {
        Duration refillDuration = parseDuration(
            rateLimitProperties.getRefillDuration(),
            rateLimitProperties.getRefillDurationUnit()
        );

        Refill refill = Refill.intervally(
            rateLimitProperties.getRefillTokens(),
            refillDuration
        );

        Bandwidth limit = Bandwidth.classic(
            rateLimitProperties.getCapacity(),
            refill
        );

        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    private Duration parseDuration(int duration, String unit) {
        return switch (unit.toUpperCase()) {
            case "SECONDS" -> Duration.ofSeconds(duration);
            case "MINUTES" -> Duration.ofMinutes(duration);
            case "HOURS" -> Duration.ofHours(duration);
            case "DAYS" -> Duration.ofDays(duration);
            default -> Duration.ofSeconds(duration);
        };
    }
}

