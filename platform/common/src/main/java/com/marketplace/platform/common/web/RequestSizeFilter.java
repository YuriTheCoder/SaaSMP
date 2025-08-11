package com.marketplace.platform.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestSizeFilter extends OncePerRequestFilter {

    private final long maxBytes;

    public RequestSizeFilter(@Value("${security.request.max-bytes:1048576}") long maxBytes) {
        this.maxBytes = maxBytes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long contentLength = request.getContentLengthLong();
        if (contentLength > 0 && contentLength > maxBytes) {
            response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            response.setHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json");
            response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Payload too large\",\"status\":413}");
            return;
        }
        // Fallback to header check if content-length is not provided by the container
        String lengthHeader = request.getHeader(HttpHeaders.CONTENT_LENGTH);
        if (lengthHeader != null) {
            try {
                long length = Long.parseLong(lengthHeader);
                if (length > maxBytes) {
                    response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
                    response.setHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json");
                    response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Payload too large\",\"status\":413}");
                    return;
                }
            } catch (NumberFormatException ignored) {
                // continue if invalid header, downstream can still enforce
            }
        }
        filterChain.doFilter(request, response);
    }
}


