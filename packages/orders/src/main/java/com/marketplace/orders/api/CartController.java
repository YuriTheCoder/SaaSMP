package com.marketplace.orders.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/carts")
public class CartController {

    @PostMapping
    public ResponseEntity<Map<String, String>> createCart() {
        String id = UUID.randomUUID().toString();
        return ResponseEntity.created(URI.create("/v1/carts/" + id + "/items"))
                .body(Map.of("id", id, "items", "/v1/carts/" + id + "/items"));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<Map<String, Object>> addItem(@PathVariable("id") String id, @Valid @RequestBody Map<String, Object> body) {
        if (!body.containsKey("sku") || !body.containsKey("qty")) {
            return ResponseEntity.unprocessableEntity().body(Map.of(
                    "type", "https://errors.marketplace.dev/cart-item",
                    "title", "Invalid cart item",
                    "status", 422,
                    "detail", "Missing sku or qty"
            ));
        }
        return ResponseEntity.ok(Map.of("cartId", id, "item", body));
    }
}


