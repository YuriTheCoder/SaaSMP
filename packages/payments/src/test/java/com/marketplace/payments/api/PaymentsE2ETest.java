package com.marketplace.payments.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {TestSecurityConfig.class})
@ActiveProfiles("test")
class PaymentsE2ETest {

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
    void create_intent_and_confirm() {
        HttpHeaders h = headers();
        h.set("Idempotency-Key", "idem-1");
        Map<String, Object> body = Map.of(
                "amount", 2590,
                "currency", "BRL",
                "method", Map.of("type", "pix"),
                "orderId", "ord_123"
        );
        ResponseEntity<Map> create = rest.exchange(url("/v1/payment-intents"), HttpMethod.POST, new HttpEntity<>(body, h), Map.class);
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String id = (String) create.getBody().get("id");

        ResponseEntity<Map> confirm = rest.exchange(url("/v1/payment-intents/" + id + "/confirm"), HttpMethod.POST, new HttpEntity<>(headers()), Map.class);
        assertThat(confirm.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(confirm.getBody().get("status")).isEqualTo("succeeded");
    }

    @Test
    void webhook_hmac_validation() {
        String secret = "change-me"; // default used by controller when env not set
        String payload = "{\"event\":\"payment.succeeded\"}";
        String timestamp = String.valueOf(System.currentTimeMillis());
        String signed = timestamp + "." + payload;
        String sig = hmac(secret, signed);

        HttpHeaders h = new HttpHeaders();
        h.set("X-Tenant-Id", "test-tenant");
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set("X-Timestamp", timestamp);
        h.set("X-Signature", sig);

        ResponseEntity<Void> res = rest.exchange(url("/v1/webhooks/payments"), HttpMethod.POST, new HttpEntity<>(payload, h), Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    private static String hmac(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }
}


