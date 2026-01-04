package com.flight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flight.dto.ErrorResponse;
import io.github.bucket4j.Bucket;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Rate limiting filter for /api/v1/flights/search endpoint
 * 
 * Note: This implementation uses in-memory rate limiting (Bucket4j).
 * For distributed deployments (Kubernetes, ECS), consider using Redis-backed rate limiting.
 * See documentation for distributed rate limiting considerations.
 */
public class RateLimitFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    
    private final Bucket searchEndpointBucket;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(Bucket searchEndpointBucket) {
        this.searchEndpointBucket = searchEndpointBucket;
        this.objectMapper = new ObjectMapper();
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
        String clientIp = getClientIpAddress(httpRequest);
        
        // Apply rate limiting only to /api/v1/flights/search endpoint
        if (path.equals("/api/v1/flights/search")) {
            if (!searchEndpointBucket.tryConsume(1)) {
                // Structured logging for rate limit violations
                logger.warn("Rate limit exceeded - Endpoint: {}, ClientIP: {}, Method: {}, UserAgent: {}", 
                    path, clientIp, httpRequest.getMethod(), httpRequest.getHeader("User-Agent"));
                
                // Return structured error response
                ErrorResponse errorResponse = new ErrorResponse(
                    "RATE_LIMIT_EXCEEDED",
                    "Rate limit exceeded. Please try again later."
                );
                
                httpResponse.setStatus(HTTP_TOO_MANY_REQUESTS);
                httpResponse.setContentType("application/json");
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                return;
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Extract client IP address from request, considering proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}

