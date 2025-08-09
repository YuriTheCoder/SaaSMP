package com.marketplace.orders.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "security.request.max-bytes=10"
})
class OrdersSecurityNegativeTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void missing_tenant_header_returns_400() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> res = rest.exchange(url("/v1/carts"), HttpMethod.POST, new HttpEntity<>("{}", h), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void invalid_tenant_header_returns_400() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Tenant-Id", "!!");
        h.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> res = rest.exchange(url("/v1/carts"), HttpMethod.POST, new HttpEntity<>("{}", h), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void payload_too_large_returns_413() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Tenant-Id", "test-tenant");
        h.setContentType(MediaType.APPLICATION_JSON);
        // set a fake large content-length to trigger RequestSizeFilter
        h.set(HttpHeaders.CONTENT_LENGTH, "999999");
        ResponseEntity<String> res = rest.exchange(url("/v1/carts"), HttpMethod.POST, new HttpEntity<>("{}", h), String.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
    }
}


