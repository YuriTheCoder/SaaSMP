package com.marketplace.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityHeadersConfig {
    @Bean
    public WebFilter securityHeadersWebFilter() {
        return (ServerWebExchange exchange, org.springframework.web.server.WebFilterChain chain) -> {
            ServerHttpResponse res = exchange.getResponse();
            res.getHeaders().addIfAbsent("X-Content-Type-Options", "nosniff");
            res.getHeaders().addIfAbsent("X-Frame-Options", "DENY");
            res.getHeaders().addIfAbsent("Referrer-Policy", "no-referrer");
            res.getHeaders().addIfAbsent("Permissions-Policy", "geolocation=() ");
            return chain.filter(exchange).then(Mono.empty());
        };
    }
}


