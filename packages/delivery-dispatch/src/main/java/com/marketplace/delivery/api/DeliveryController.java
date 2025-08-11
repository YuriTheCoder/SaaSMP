package com.marketplace.delivery.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/deliveries")
public class DeliveryController {

    @PostMapping("/dispatch")
    public ResponseEntity<Map<String, Object>> dispatch(@RequestBody Map<String, Object> body) {
        String id = "del_" + UUID.randomUUID();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", id, "status", "ASSIGNED", "eta", 12));
    }

    @GetMapping("/{id}/track")
    public ResponseEntity<Map<String, Object>> track(@PathVariable("id") String id) {
        return ResponseEntity.ok(Map.of("id", id, "wsUrl", "ws://localhost:8084/track/" + id));
    }

    @PostMapping("/{id}/reassign")
    public ResponseEntity<Map<String, Object>> reassign(@PathVariable("id") String id) {
        return ResponseEntity.ok(Map.of("id", id, "status", "REASSIGNED"));
    }
}


