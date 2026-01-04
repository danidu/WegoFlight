package com.flight.config;

import io.github.bucket4j.Bucket;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public RateLimitFilter rateLimitFilter(Bucket searchEndpointBucket) {
        return new RateLimitFilter(searchEndpointBucket);
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(RateLimitFilter rateLimitFilter) {
        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(rateLimitFilter);
        registration.addUrlPatterns("/api/v1/flights/search");
        registration.setName("rateLimitFilter");
        registration.setOrder(1);
        return registration;
    }
}

