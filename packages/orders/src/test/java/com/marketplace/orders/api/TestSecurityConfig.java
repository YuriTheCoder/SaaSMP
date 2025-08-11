package com.marketplace.orders.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

@TestConfiguration
public class TestSecurityConfig {
    @Bean
    @Order(0)
    SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    RestTemplateBuilder restTemplateBuilder() {
        // Use Apache HttpClient 5 to fully support PATCH and avoid follow-up loop
        CloseableHttpClient client = HttpClients.custom().disableRedirectHandling().build();
        return new RestTemplateBuilder().requestFactory(() -> new HttpComponentsClientHttpRequestFactory(client));
    }
}


