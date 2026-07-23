package com.insert7team.TicketWave.fraud.controller;

import com.insert7team.TicketWave.shared.domain.dto.ApiResponse;
import com.insert7team.TicketWave.fraud.entity.FraudAlert;
import com.insert7team.TicketWave.fraud.service.FraudDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/fraud")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;

    public FraudController(FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<FraudAlert>>> getUnresolvedAlerts() {
        List<FraudAlert> alerts = fraudDetectionService.getUnresolvedAlerts();
        return ResponseEntity.ok(ApiResponse.ok(alerts));
    }

    @PostMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<ApiResponse<Void>> resolveAlert(
            @PathVariable Long alertId,
            @AuthenticationPrincipal UserDetails userDetails) {
        fraudDetectionService.resolveAlert(alertId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Alert resolved", null));
    }
}
