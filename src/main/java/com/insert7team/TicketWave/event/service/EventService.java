package com.insert7team.TicketWave.event.service;

import com.insert7team.TicketWave.event.dto.*;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventResponse createEvent(Long organizerId, CreateEventRequest request);
    EventResponse updateEvent(Long eventId, UpdateEventRequest request);
    EventResponse getEvent(Long eventId);
    void cancelEvent(Long eventId, String reason);
    void postponeEvent(Long eventId, LocalDateTime newDate);
    EventPricingResponse setPricing(Long eventId, EventPricingRequest request);
    List<EventPricingResponse> getEventPricing(Long eventId);
    List<EventResponse> getOrganizerEvents(Long organizerId);
}
