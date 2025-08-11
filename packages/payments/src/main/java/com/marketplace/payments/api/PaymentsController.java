package com.marketplace.payments.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@Validated
@RequestMapping("/v1")
public class PaymentsController {

    @PostMapping("/payment-intents")
    public ResponseEntity<Map<String, Object>> createPaymentIntent(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @Valid @RequestBody Map<String, Object> body
    ) {
        String id = "pi_" + UUID.randomUUID();
        return ResponseEntity.status(HttpStatus.CREATED)
                .eTag(UUID.randomUUID().toString())
                .location(URI.create("/v1/payment-intents/" + id))
                .body(Map.of(
                        "id", id,
                        "status", "requires_confirmation",
                        "amount", body.getOrDefault("amount", 0),
                        "currency", body.getOrDefault("currency", "BRL"),
                        "method", body.get("method"),
                        "orderId", body.get("orderId")
                ));
    }

    @PostMapping("/payment-intents/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirm(@PathVariable("id") String id) {
        return ResponseEntity.ok(Map.of("id", id, "status", "succeeded"));
    }

    @PostMapping("/refunds")
    public ResponseEntity<Map<String, Object>> refund(@RequestBody Map<String, Object> body) {
        String refundId = "re_" + UUID.randomUUID();
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", refundId,
                "status", "succeeded",
                "payment_intent", body.get("payment_intent"),
                "amount", body.getOrDefault("amount", 0)
        ));
    }

    @PostMapping("/webhooks/payments")
    public ResponseEntity<Void> webhooks(@RequestHeader(value = "X-Timestamp", required = false) String timestamp,
                                         @RequestHeader(value = "X-Signature", required = false) String signature,
                                         @RequestBody String payload) {
        // Minimal HMAC SHA256 signature check (demo only)
        String secret = System.getenv().getOrDefault("WEBHOOK_SECRET", "change-me");
        if (timestamp == null || signature == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String signed = timestamp + "." + payload;
        String expected = hmacSha256Hex(secret, signed);
        if (!expected.equalsIgnoreCase(signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    private static String hmacSha256Hex(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC", e);
        }
    }
}


