package com.insert7team.TicketWave.venue.controller;

import com.insert7team.TicketWave.common.dto.ApiResponse;
import com.insert7team.TicketWave.common.dto.PagedResponse;
import com.insert7team.TicketWave.venue.dto.*;
import com.insert7team.TicketWave.venue.service.VenueService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VenueResponse>> createVenue(@Valid @RequestBody VenueRequest request) {
        VenueResponse response = venueService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Venue created", response));
    }

    @GetMapping("/{venueId}")
    public ResponseEntity<ApiResponse<VenueResponse>> getVenue(@PathVariable Long venueId) {
        VenueResponse response = venueService.getVenue(venueId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{venueId}")
    public ResponseEntity<ApiResponse<VenueResponse>> updateVenue(@PathVariable Long venueId,
                                                                    @Valid @RequestBody VenueRequest request) {
        VenueResponse response = venueService.updateVenue(venueId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<VenueResponse>>> getVenuesByCity(
            @RequestParam String city, Pageable pageable) {
        PagedResponse<VenueResponse> response = venueService.getVenuesByCity(city, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{venueId}/seatmap")
    public ResponseEntity<ApiResponse<SeatMapResponse>> getSeatMap(@PathVariable Long venueId) {
        SeatMapResponse response = venueService.getSeatMap(venueId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{venueId}/sections")
    public ResponseEntity<ApiResponse<SectionResponse>> addSection(@PathVariable Long venueId,
                                                                    @Valid @RequestBody SectionRequest request) {
        SectionResponse response = venueService.addSection(venueId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Section added", response));
    }

    @PostMapping("/{venueId}/sections/{sectionId}/seats/generate")
    public ResponseEntity<ApiResponse<Void>> generateSeats(@PathVariable Long venueId,
                                                            @PathVariable Long sectionId,
                                                            @RequestParam int rows,
                                                            @RequestParam int seatsPerRow) {
        venueService.generateSeats(sectionId, rows, seatsPerRow);
        return ResponseEntity.ok(ApiResponse.ok("Seats generated", null));
    }
}
