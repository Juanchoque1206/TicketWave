package com.insert7team.TicketWave.promotion.controller;

import com.insert7team.TicketWave.common.dto.ApiResponse;
import com.insert7team.TicketWave.common.enums.PromotionScope;
import com.insert7team.TicketWave.promotion.dto.CreatePromotionRequest;
import com.insert7team.TicketWave.promotion.dto.PromotionResponse;
import com.insert7team.TicketWave.promotion.dto.PromotionValidationResponse;
import com.insert7team.TicketWave.promotion.service.PromotionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PromotionResponse>> createPromotion(
            @Valid @RequestBody CreatePromotionRequest request) {
        PromotionResponse response = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Promotion created", response));
    }

    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<PromotionValidationResponse>> validatePromotion(
            @RequestParam String code,
            @RequestParam(required = false) Long eventId,
            @RequestParam BigDecimal amount) {
        PromotionValidationResponse response = promotionService.validatePromotion(code, eventId, amount);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getActivePromotions(
            @RequestParam PromotionScope scope) {
        List<PromotionResponse> response = promotionService.getActivePromotions(scope);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<ApiResponse<List<PromotionResponse>>> getVenuePromotions(@PathVariable Long venueId) {
        List<PromotionResponse> response = promotionService.getVenuePromotions(venueId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{promotionId}")
    public ResponseEntity<ApiResponse<Void>> deactivatePromotion(@PathVariable Long promotionId) {
        promotionService.deactivatePromotion(promotionId);
        return ResponseEntity.ok(ApiResponse.ok("Promotion deactivated", null));
    }
}
