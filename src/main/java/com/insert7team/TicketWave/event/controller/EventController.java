package com.insert7team.TicketWave.event.controller;

import com.insert7team.TicketWave.shared.domain.dto.ApiResponse;
import com.insert7team.TicketWave.event.dto.*;
import com.insert7team.TicketWave.event.service.EventService;
import com.insert7team.TicketWave.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;

    public EventController(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateEventRequest request) {
        Long organizerId = userService.getUserEntityByEmail(userDetails.getUsername()).getId();
        EventResponse response = eventService.createEvent(organizerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Event created", response));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> getEvent(@PathVariable Long eventId) {
        EventResponse response = eventService.getEvent(eventId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(@PathVariable Long eventId,
                                                                    @RequestBody UpdateEventRequest request) {
        EventResponse response = eventService.updateEvent(eventId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{eventId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelEvent(@PathVariable Long eventId,
                                                          @RequestBody Map<String, String> body) {
        eventService.cancelEvent(eventId, body.getOrDefault("reason", "No reason provided"));
        return ResponseEntity.ok(ApiResponse.ok("Event cancelled", null));
    }

    @PostMapping("/{eventId}/postpone")
    public ResponseEntity<ApiResponse<Void>> postponeEvent(@PathVariable Long eventId,
                                                            @RequestBody Map<String, LocalDateTime> body) {
        eventService.postponeEvent(eventId, body.get("newDate"));
        return ResponseEntity.ok(ApiResponse.ok("Event postponed", null));
    }

    @PostMapping("/{eventId}/pricing")
    public ResponseEntity<ApiResponse<EventPricingResponse>> setPricing(@PathVariable Long eventId,
                                                                        @Valid @RequestBody EventPricingRequest request) {
        EventPricingResponse response = eventService.setPricing(eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Pricing set", response));
    }

    @GetMapping("/{eventId}/pricing")
    public ResponseEntity<ApiResponse<List<EventPricingResponse>>> getEventPricing(@PathVariable Long eventId) {
        List<EventPricingResponse> response = eventService.getEventPricing(eventId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/organizer/me")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getOrganizerEvents(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long organizerId = userService.getUserEntityByEmail(userDetails.getUsername()).getId();
        List<EventResponse> response = eventService.getOrganizerEvents(organizerId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
