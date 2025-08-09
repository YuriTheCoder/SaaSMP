package com.marketplace.orders.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestSecurityConfig.class}, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
@ActiveProfiles("test")
class OrdersE2ETest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    private HttpHeaders headers() {
        HttpHeaders h = new HttpHeaders();
        h.set("X-Tenant-Id", "test-tenant");
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    @Test
    void cart_flow_checkout_and_status() {
        ResponseEntity<Map> create = rest.exchange(url("/v1/carts"), HttpMethod.POST, new HttpEntity<>(headers()), Map.class);
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String itemsUrl = (String) create.getBody().get("items");
        Map<String, Object> item = Map.of("sku", "SKU-1", "qty", 2);
        ResponseEntity<Map> addItem = rest.exchange(url(itemsUrl), HttpMethod.POST, new HttpEntity<>(item, headers()), Map.class);
        assertThat(addItem.getStatusCode()).isEqualTo(HttpStatus.OK);

        String cartId = (String) addItem.getBody().get("cartId");
        ResponseEntity<Map> checkout = rest.exchange(url("/v1/carts/" + cartId + "/checkout"), HttpMethod.POST, new HttpEntity<>(Map.of(), headers()), Map.class);
        assertThat(checkout.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String orderId = (String) checkout.getBody().get("id");
        ResponseEntity<Map> status = rest.exchange(url("/v1/orders/" + orderId + "/status"), HttpMethod.PATCH, new HttpEntity<>(Map.of("status", "CONFIRMED"), headers()), Map.class);
        assertThat(status.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private String url(String path) {
        if (path.startsWith("/")) {
            return "http://localhost:" + port + path;
        }
        return "http://localhost:" + port + path; // already includes /v1/...
    }

}


