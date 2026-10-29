package com.klu.alert.controller;

import com.klu.alert.entity.Alert;
import com.klu.alert.service.AlertService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // 1. Create a new alert
    @PostMapping
    public ResponseEntity<Alert> createAlert(
            @RequestBody Alert alert) {

        Alert savedAlert = alertService.createAlert(alert);

        return ResponseEntity.ok(savedAlert);
    }

    // 2. Get all alerts
    @GetMapping
    public ResponseEntity<List<Alert>> getAllAlerts() {

        return ResponseEntity.ok(
                alertService.getAllAlerts()
        );
    }

    // 3. Get alert by ID
    @GetMapping("/{alertId}")
    public ResponseEntity<Alert> getAlertById(
            @PathVariable Long alertId) {

        return alertService.getAlertById(alertId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Get alerts for a particular meter
    @GetMapping("/meter/{meterId}")
    public ResponseEntity<List<Alert>> getAlertsByMeterId(
            @PathVariable Long meterId) {

        return ResponseEntity.ok(
                alertService.getAlertsByMeterId(meterId)
        );
    }

    // 5. Update an existing alert
    @PutMapping("/{alertId}")
    public ResponseEntity<Alert> updateAlert(
            @PathVariable Long alertId,
            @RequestBody Alert alert) {

        try {
            Alert updatedAlert =
                    alertService.updateAlert(alertId, alert);

            return ResponseEntity.ok(updatedAlert);

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 6. Delete an alert
    @DeleteMapping("/{alertId}")
    public ResponseEntity<String> deleteAlert(
            @PathVariable Long alertId) {

        try {
            alertService.deleteAlert(alertId);

            return ResponseEntity.ok(
                    "Alert deleted successfully"
            );

        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 7. Check consumption against the configured threshold
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkThreshold(
            @RequestParam Long meterId,
            @RequestParam Double consumption) {

        boolean triggered =
                alertService.checkThreshold(
                        meterId, consumption
                );

        Map<String, Object> result = new HashMap<>();

        result.put("meterId", meterId);
        result.put("consumption", consumption);
        result.put("thresholdExceeded", triggered);
        result.put(
                "message",
                triggered
                        ? "High consumption detected"
                        : "Consumption is within threshold"
        );

        return ResponseEntity.ok(result);
    }
}