package com.marketplace.platform.common.security;

import com.marketplace.platform.common.web.RequestIdFilter;
import com.marketplace.platform.common.web.RequestSizeFilter;
import com.marketplace.platform.common.web.TenantFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@AutoConfiguration
@Import({TenantFilter.class, RequestIdFilter.class, RequestSizeFilter.class})
public class ApiSecurityAutoConfiguration {

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${security.cors.allowed-origins:}") List<String> allowedOrigins) {
        CorsConfiguration cfg = new CorsConfiguration();
        if (allowedOrigins != null) {
            cfg.setAllowedOrigins(allowedOrigins);
        }
        cfg.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Request-Id", "X-Tenant-Id", "Idempotency-Key"));
        cfg.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    @ConditionalOnProperty(name = "security.auth.enabled", havingValue = "true")
    public SecurityFilterChain securityFilterChainSecured(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/v3/api-docs/**", "/swagger-ui/**").denyAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .xssProtection(x -> {})
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'"))
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
                .contentTypeOptions(contentType -> {})
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    @ConditionalOnProperty(name = "security.auth.enabled", havingValue = "false", matchIfMissing = true)
    public SecurityFilterChain securityFilterChainOpen(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .headers(headers -> headers
                .xssProtection(x -> {})
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'"))
                .frameOptions(frame -> frame.deny())
                .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
                .contentTypeOptions(contentType -> {})
            );
        return http.build();
    }
}


