package com.sentinel.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RateLimiterFilterTest {

    private RateLimiterFilter rateLimiterFilter;
    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private FilterChain mockFilterChain;
    private StringWriter stringWriter;
    private PrintWriter printWriter;

    @BeforeEach
    void setUp() throws IOException {
        rateLimiterFilter = new RateLimiterFilter();
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockFilterChain = mock(FilterChain.class);
        
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        when(mockResponse.getWriter()).thenReturn(printWriter);
    }

    @Test
    void testDoFilter_WhenPermitAvailable_Proceeds() throws IOException, ServletException {
        // Act - first request, should easily pass
        rateLimiterFilter.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Assert
        verify(mockFilterChain, times(1)).doFilter(mockRequest, mockResponse);
        verify(mockResponse, never()).setStatus(429);
    }

    @Test
    void testDoFilter_WhenPermitExhausted_Returns429TooManyRequests() throws IOException, ServletException {
        // Act - spam requests to exhaust the 50 permits/sec limit
        int passed = 0;
        int blocked = 0;
        
        for (int i = 0; i < 60; i++) {
            HttpServletResponse tempMockResponse = mock(HttpServletResponse.class);
            StringWriter tempStringWriter = new StringWriter();
            when(tempMockResponse.getWriter()).thenReturn(new PrintWriter(tempStringWriter));
            FilterChain tempFilterChain = mock(FilterChain.class);

            rateLimiterFilter.doFilter(mockRequest, tempMockResponse, tempFilterChain);
            
            try {
                verify(tempFilterChain, times(1)).doFilter(mockRequest, tempMockResponse);
                passed++;
            } catch (AssertionError e) {
                // If it wasn't called, we expect the status 429
                verify(tempMockResponse, times(1)).setStatus(429);
                blocked++;
                assertTrue(tempStringWriter.toString().contains("Rate limit exceeded"));
            }
        }

        // Assert
        assertTrue(passed > 0, "At least some requests should pass");
        assertTrue(blocked > 0, "Rate limiter should have blocked excess requests");
        assertTrue(passed <= 50, "Limit should not exceed 50 per second");
    }
}
