package com.marketplace.orders.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class OrderController {

    @PostMapping("/carts/{id}/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@PathVariable("id") String id,
                                                        @Valid @RequestBody Map<String, Object> request) {
        String orderId = "ord_" + UUID.randomUUID();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", orderId, "cartId", id, "status", "PLACED"));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable("id") String id, @RequestBody Map<String, String> body) {
        String newStatus = body.getOrDefault("status", "");
        if (newStatus.isBlank()) {
            return ResponseEntity.unprocessableEntity().body(Map.of(
                    "type", "https://errors.marketplace.dev/order-status",
                    "title", "Invalid transition",
                    "status", 422,
                    "code", "ORDER_TRANSITION_INVALID",
                    "detail", "Missing or invalid status"
            ));
        }
        return ResponseEntity.ok(Map.of("id", id, "status", newStatus));
    }
}


