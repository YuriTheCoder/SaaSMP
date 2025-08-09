package com.marketplace.platform.common.web;

import com.marketplace.platform.common.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {
    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tenantId = request.getHeader(HEADER_TENANT_ID);
        if (tenantId == null || tenantId.isBlank() || !tenantId.matches("^[a-zA-Z0-9_-]{3,64}$")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setHeader(HttpHeaders.CONTENT_TYPE, "application/problem+json");
            response.getWriter().write("{\"type\":\"about:blank\",\"title\":\"Invalid tenant header\",\"status\":400,\"detail\":\"Header X-Tenant-Id is required and must be 3-64 chars [a-zA-Z0-9_-]\"}");
            return;
        }
        try {
            TenantContext.setTenantId(tenantId);
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}


