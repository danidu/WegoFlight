package com.flight.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flight.dto.ErrorResponse;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RateLimitFilterTest {

    @Mock
    private Bucket bucket;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RateLimitFilter rateLimitFilter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        rateLimitFilter = new RateLimitFilter(bucket);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testRateLimitExceeded() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/flights/search");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader(anyString())).thenReturn(null);
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(bucket.tryConsume(1)).thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        rateLimitFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setStatus(429);
        verify(response).setContentType("application/json");
        verify(response).setCharacterEncoding("UTF-8");
        verify(filterChain, never()).doFilter(any(), any());

        String responseBody = stringWriter.toString();
        ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
        assertEquals("RATE_LIMIT_EXCEEDED", errorResponse.getCode());
        assertNotNull(errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void testRateLimitNotExceeded() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/flights/search");
        when(bucket.tryConsume(1)).thenReturn(true);

        // When
        rateLimitFilter.doFilter(request, response, filterChain);

        // Then
        verify(response, never()).setStatus(429);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testOtherEndpointsNotRateLimited() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/flights/other");

        // When
        rateLimitFilter.doFilter(request, response, filterChain);

        // Then
        verify(bucket, never()).tryConsume(anyInt());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testClientIpExtractionFromXForwardedFor() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/flights/search");
        when(request.getMethod()).thenReturn("POST");
        when(request.getHeader("User-Agent")).thenReturn("Test-Agent");
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.1, 10.0.0.1");
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(bucket.tryConsume(1)).thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // When
        rateLimitFilter.doFilter(request, response, filterChain);

        // Then
        verify(response).setStatus(429);
        verify(request).getHeader("X-Forwarded-For");
        // Verify logging includes client IP (would need to verify logger calls)
    }
}

