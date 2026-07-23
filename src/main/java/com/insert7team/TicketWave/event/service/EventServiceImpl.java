package com.insert7team.TicketWave.event.service;

import com.insert7team.TicketWave.shared.infrastructure.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.event.dto.*;
import com.insert7team.TicketWave.event.entity.Event;
import com.insert7team.TicketWave.event.entity.EventPricing;
import com.insert7team.TicketWave.event.kafka.EventCancelledEvent;
import com.insert7team.TicketWave.event.kafka.EventChangedEvent;
import com.insert7team.TicketWave.event.kafka.EventKafkaProducer;
import com.insert7team.TicketWave.event.repository.EventPricingRepository;
import com.insert7team.TicketWave.event.repository.EventRepository;
import com.insert7team.TicketWave.venue.service.VenueService;
import com.insert7team.TicketWave.venue.dto.VenueResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventPricingRepository eventPricingRepository;
    private final VenueService venueService;
    private final EventKafkaProducer eventKafkaProducer;

    public EventServiceImpl(EventRepository eventRepository, EventPricingRepository eventPricingRepository,
                            VenueService venueService, EventKafkaProducer eventKafkaProducer) {
        this.eventRepository = eventRepository;
        this.eventPricingRepository = eventPricingRepository;
        this.venueService = venueService;
        this.eventKafkaProducer = eventKafkaProducer;
    }

    @Override
    @Transactional
    public EventResponse createEvent(Long organizerId, CreateEventRequest request) {
        VenueResponse venue = venueService.getVenue(request.venueId());

        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setArtist(request.artist());
        event.setCategory(request.category());
        event.setEventDate(request.eventDate());
        event.setDoorsOpenAt(request.doorsOpenAt());
        event.setStatus(com.insert7team.TicketWave.event.domain.EventStatus.DRAFT);
        event.setMaxTicketsPerUser(request.maxTicketsPerUser() > 0 ? request.maxTicketsPerUser() : 6);
        event.setSalesStartAt(request.salesStartAt());
        event.setSalesEndAt(request.salesEndAt());
        event.setVenueId(request.venueId());
        event.setVenueName(venue.name());
        event.setVenueCity(venue.city());
        event.setOrganizerId(organizerId);
        event.setImageUrl(request.imageUrl());
        event = eventRepository.save(event);
        return toEventResponse(event);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"eventDetail", "eventSearch", "upcomingEvents"}, allEntries = true)
    public EventResponse updateEvent(Long eventId, UpdateEventRequest request) {
        Event event = getEventEntity(eventId);
        event.updateDetails(request.title(), request.description(), request.artist(),
                request.category(), request.eventDate(), request.doorsOpenAt(), request.imageUrl());
        event = eventRepository.save(event);

        eventKafkaProducer.publishEventChanged(new EventChangedEvent(
                UUID.randomUUID().toString(), "EVENT_CHANGED", LocalDateTime.now(),
                event.getId(), event.getTitle(), "DETAILS_UPDATED", "Event details updated", 0
        ));

        return toEventResponse(event);
    }

    @Override
    @Cacheable(value = "eventDetail", key = "#eventId")
    public EventResponse getEvent(Long eventId) {
        Event event = getEventEntity(eventId);
        return toEventResponse(event);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"eventDetail", "eventSearch", "upcomingEvents"}, allEntries = true)
    public void cancelEvent(Long eventId, String reason) {
        Event event = getEventEntity(eventId);
        event.cancel(reason);
        eventRepository.save(event);

        eventKafkaProducer.publishEventCancelled(new EventCancelledEvent(
                UUID.randomUUID().toString(), "EVENT_CANCELLED", LocalDateTime.now(),
                event.getId(), event.getTitle(), reason, List.of()
        ));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"eventDetail", "eventSearch", "upcomingEvents"}, allEntries = true)
    public void postponeEvent(Long eventId, LocalDateTime newDate) {
        Event event = getEventEntity(eventId);
        event.postpone(newDate);
        eventRepository.save(event);

        eventKafkaProducer.publishEventChanged(new EventChangedEvent(
                UUID.randomUUID().toString(), "EVENT_CHANGED", LocalDateTime.now(),
                event.getId(), event.getTitle(), "DATE_CHANGED", "Event postponed to " + newDate, 0
        ));
    }

    @Override
    @Transactional
    public EventPricingResponse setPricing(Long eventId, EventPricingRequest request) {
        Event event = getEventEntity(eventId);

        EventPricing pricing = eventPricingRepository
                .findByEventIdAndSectionIdAndTicketType(eventId, request.sectionId(), request.ticketType())
                .orElse(new EventPricing());

        pricing.setEvent(event);
        pricing.setSectionId(request.sectionId());
        pricing.setSectionName(request.sectionName());
        pricing.setTicketType(request.ticketType());
        pricing.setPrice(request.price());
        pricing.setCurrency(request.currency() != null ? request.currency() : "USD");
        pricing.setAvailableQuantity(request.availableQuantity());
        pricing = eventPricingRepository.save(pricing);

        return toEventPricingResponse(pricing);
    }

    @Override
    public List<EventPricingResponse> getEventPricing(Long eventId) {
        return eventPricingRepository.findByEventId(eventId).stream()
                .map(this::toEventPricingResponse)
                .toList();
    }

    @Override
    public List<EventResponse> getOrganizerEvents(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId).stream()
                .map(this::toEventResponse)
                .toList();
    }

    public Event getEventEntity(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
    }

    private EventResponse toEventResponse(Event event) {
        List<EventPricingResponse> pricings = event.getPricings().stream()
                .map(this::toEventPricingResponse)
                .toList();
        return new EventResponse(
                event.getId(), event.getTitle(), event.getDescription(), event.getArtist(),
                event.getCategory(), event.getEventDate(), event.getDoorsOpenAt(), event.getStatus(),
                event.getMaxTicketsPerUser(), event.getSalesStartAt(), event.getSalesEndAt(),
                event.getVenueId(), event.getVenueName(), event.getVenueCity(),
                event.getOrganizerId(), event.getImageUrl(), pricings, event.getCreatedAt()
        );
    }

    private EventPricingResponse toEventPricingResponse(EventPricing pricing) {
        return new EventPricingResponse(
                pricing.getId(), pricing.getSectionId(), pricing.getSectionName(),
                pricing.getTicketType(), pricing.getPrice(), pricing.getCurrency(),
                pricing.getAvailableQuantity()
        );
    }
}
