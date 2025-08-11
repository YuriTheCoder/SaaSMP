package com.marketplace.delivery.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestSecurityConfig.class})
@ActiveProfiles("test")
class DeliveryE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Tenant-Id", "test-tenant");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void dispatch_track_reassign() {
        ResponseEntity<Map> dispatch = rest.exchange(url("/v1/deliveries/dispatch"), HttpMethod.POST, new HttpEntity<>(Map.of("orderId", "ord_1"), headers()), Map.class);
        assertThat(dispatch.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String id = (String) dispatch.getBody().get("id");

        ResponseEntity<Map> track = rest.exchange(url("/v1/deliveries/" + id + "/track"), HttpMethod.GET, new HttpEntity<>(headers()), Map.class);
        assertThat(track.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(track.getBody().get("wsUrl")).isNotNull();

        ResponseEntity<Map> reassign = rest.exchange(url("/v1/deliveries/" + id + "/reassign"), HttpMethod.POST, new HttpEntity<>(headers()), Map.class);
        assertThat(reassign.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reassign.getBody().get("status")).isEqualTo("REASSIGNED");
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}


