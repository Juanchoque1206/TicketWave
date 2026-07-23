package com.insert7team.TicketWave.event.controller;

import com.insert7team.TicketWave.shared.domain.dto.ApiResponse;
import com.insert7team.TicketWave.shared.domain.dto.PagedResponse;
import com.insert7team.TicketWave.event.domain.EventCategory;
import com.insert7team.TicketWave.event.dto.EventSearchCriteria;
import com.insert7team.TicketWave.event.dto.EventSummaryResponse;
import com.insert7team.TicketWave.event.service.EventSearchService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class EventSearchController {

    private final EventSearchService eventSearchService;

    public EventSearchController(EventSearchService eventSearchService) {
        this.eventSearchService = eventSearchService;
    }

    @GetMapping("/events")
    public ResponseEntity<ApiResponse<PagedResponse<EventSummaryResponse>>> searchEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo,
            @RequestParam(required = false) EventCategory category,
            Pageable pageable) {
        EventSearchCriteria criteria = new EventSearchCriteria(city, artist, venueId, dateFrom, dateTo, category);
        PagedResponse<EventSummaryResponse> response = eventSearchService.searchEvents(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/events/upcoming")
    public ResponseEntity<ApiResponse<List<EventSummaryResponse>>> getUpcomingEvents(
            @RequestParam(defaultValue = "10") int limit) {
        List<EventSummaryResponse> response = eventSearchService.getUpcomingEvents(limit);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
