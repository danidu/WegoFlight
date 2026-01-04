package com.flight.config;

import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class RateLimitFilter implements Filter {

    private final Bucket searchEndpointBucket;

    public RateLimitFilter(Bucket searchEndpointBucket) {
        this.searchEndpointBucket = searchEndpointBucket;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        
        // Apply rate limiting only to /api/v1/flights/search endpoint
        if (path.equals("/api/v1/flights/search")) {
            if (!searchEndpointBucket.tryConsume(1)) {
                httpResponse.setStatus(429); // HTTP 429 Too Many Requests
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write("{\"error\": \"Rate limit exceeded. Please try again later.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}

