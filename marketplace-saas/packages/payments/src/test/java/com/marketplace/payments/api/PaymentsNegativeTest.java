package com.marketplace.payments.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestSecurityConfig.class})
@ActiveProfiles("test")
class PaymentsNegativeTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void webhook_hmac_invalid_returns_401() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Tenant-Id", "test-tenant");
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("X-Timestamp", String.valueOf(System.currentTimeMillis()));
        h.set("X-Signature", "deadbeef");
        ResponseEntity<Void> res = rest.exchange(url("/v1/webhooks/payments"), HttpMethod.POST, new HttpEntity<>("{}", h), Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}


