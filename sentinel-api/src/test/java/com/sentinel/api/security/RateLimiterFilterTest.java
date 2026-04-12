package com.sentinel.api.security;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.Mockito.*;

class RateLimiterFilterTest {

    private RateLimiterFilter rateLimiterFilter;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockFilterChain;

    @BeforeEach
    void setUp() throws IOException {
        rateLimiterFilter = new RateLimiterFilter();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
    }

    @Test
    void testDoFilter_WhenPermitAvailable_Proceeds() throws IOException, ServletException {
        // Act - first request with a fresh limiter should always pass
        rateLimiterFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Assert
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockResponse, never()).setStatus(429);
    }

    @Test
    void testDoFilter_WhenPermitExhausted_Returns429TooManyRequests() throws IOException, ServletException {
        // Arrange - inject a rate limiter that allows zero permits per second
        // so that tryAcquire() will always return false immediately
        RateLimiter exhaustedLimiter = RateLimiter.create(0.001); // ~1 permit per 1000 seconds
        exhaustedLimiter.tryAcquire(); // consume the initial stored permit
        ReflectionTestUtils.setField(rateLimiterFilter, "rateLimiter", exhaustedLimiter);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(mockResponse.getWriter()).thenReturn(printWriter);

        // Act
        rateLimiterFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Assert - chain should NOT be called, response should be 429
        verify(mockFilterChain, never()).doFilter(mockRequest, mockResponse);
        verify(mockResponse, times(1)).setStatus(429);
        verify(mockResponse, times(1)).setContentType("application/json");
    }
}
