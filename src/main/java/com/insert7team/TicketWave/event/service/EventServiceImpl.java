package com.insert7team.TicketWave.event.service;

import com.insert7team.TicketWave.common.enums.EventStatus;
import com.insert7team.TicketWave.common.exception.BusinessRuleException;
import com.insert7team.TicketWave.common.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.event.dto.*;
import com.insert7team.TicketWave.event.entity.Event;
import com.insert7team.TicketWave.event.entity.EventPricing;
import com.insert7team.TicketWave.event.kafka.EventCancelledEvent;
import com.insert7team.TicketWave.event.kafka.EventChangedEvent;
import com.insert7team.TicketWave.event.kafka.EventKafkaProducer;
import com.insert7team.TicketWave.event.repository.EventPricingRepository;
import com.insert7team.TicketWave.event.repository.EventRepository;
import com.insert7team.TicketWave.user.entity.User;
import com.insert7team.TicketWave.user.service.UserService;
import com.insert7team.TicketWave.venue.dto.VenueResponse;
import com.insert7team.TicketWave.venue.entity.Section;
import com.insert7team.TicketWave.venue.entity.Venue;
import com.insert7team.TicketWave.venue.repository.SectionRepository;
import com.insert7team.TicketWave.venue.repository.VenueRepository;
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
    private final VenueRepository venueRepository;
    private final SectionRepository sectionRepository;
    private final UserService userService;
    private final EventKafkaProducer eventKafkaProducer;

    public EventServiceImpl(EventRepository eventRepository, EventPricingRepository eventPricingRepository,
                            VenueRepository venueRepository, SectionRepository sectionRepository,
                            UserService userService, EventKafkaProducer eventKafkaProducer) {
        this.eventRepository = eventRepository;
        this.eventPricingRepository = eventPricingRepository;
        this.venueRepository = venueRepository;
        this.sectionRepository = sectionRepository;
        this.userService = userService;
        this.eventKafkaProducer = eventKafkaProducer;
    }

    @Override
    @Transactional
    public EventResponse createEvent(Long organizerId, CreateEventRequest request) {
        User organizer = userService.getUserEntityById(organizerId);
        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.venueId()));

        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setArtist(request.artist());
        event.setCategory(request.category());
        event.setEventDate(request.eventDate());
        event.setDoorsOpenAt(request.doorsOpenAt());
        event.setStatus(EventStatus.DRAFT);
        event.setMaxTicketsPerUser(request.maxTicketsPerUser() > 0 ? request.maxTicketsPerUser() : 6);
        event.setSalesStartAt(request.salesStartAt());
        event.setSalesEndAt(request.salesEndAt());
        event.setVenue(venue);
        event.setOrganizer(organizer);
        event.setImageUrl(request.imageUrl());
        event = eventRepository.save(event);
        return toEventResponse(event);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"eventDetail", "eventSearch", "upcomingEvents"}, allEntries = true)
    public EventResponse updateEvent(Long eventId, UpdateEventRequest request) {
        Event event = getEventEntity(eventId);
        if (request.title() != null) event.setTitle(request.title());
        if (request.description() != null) event.setDescription(request.description());
        if (request.artist() != null) event.setArtist(request.artist());
        if (request.category() != null) event.setCategory(request.category());
        if (request.eventDate() != null) event.setEventDate(request.eventDate());
        if (request.doorsOpenAt() != null) event.setDoorsOpenAt(request.doorsOpenAt());
        if (request.imageUrl() != null) event.setImageUrl(request.imageUrl());
        event = eventRepository.save(event);

        eventKafkaProducer.publishEventChanged(new EventChangedEvent(
                UUID.randomUUID().toString(), "EVENT_CHANGED", LocalDateTime.now(),
                event.getId(), "DETAILS_UPDATED", "Event details updated", 0
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
        if (event.getStatus() == EventStatus.CANCELLED) {
            throw new BusinessRuleException("Event is already cancelled");
        }
        event.setStatus(EventStatus.CANCELLED);
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
        event.setStatus(EventStatus.POSTPONED);
        event.setEventDate(newDate);
        eventRepository.save(event);

        eventKafkaProducer.publishEventChanged(new EventChangedEvent(
                UUID.randomUUID().toString(), "EVENT_CHANGED", LocalDateTime.now(),
                event.getId(), "DATE_CHANGED", "Event postponed to " + newDate, 0
        ));
    }

    @Override
    @Transactional
    public EventPricingResponse setPricing(Long eventId, EventPricingRequest request) {
        Event event = getEventEntity(eventId);
        Section section = sectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + request.sectionId()));

        EventPricing pricing = eventPricingRepository
                .findByEventIdAndSectionIdAndTicketType(eventId, request.sectionId(), request.ticketType())
                .orElse(new EventPricing());

        pricing.setEvent(event);
        pricing.setSection(section);
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
        Venue venue = event.getVenue();
        VenueResponse venueResponse = new VenueResponse(
                venue.getId(), venue.getName(), venue.getCity(), venue.getAddress(),
                venue.getCountry(), venue.getTotalCapacity(), venue.isHasAssignedSeating(),
                venue.getSections().size(), venue.getCreatedAt()
        );
        List<EventPricingResponse> pricings = event.getPricings().stream()
                .map(this::toEventPricingResponse)
                .toList();
        return new EventResponse(
                event.getId(), event.getTitle(), event.getDescription(), event.getArtist(),
                event.getCategory(), event.getEventDate(), event.getDoorsOpenAt(), event.getStatus(),
                event.getMaxTicketsPerUser(), event.getSalesStartAt(), event.getSalesEndAt(),
                venueResponse, event.getImageUrl(), pricings, event.getCreatedAt()
        );
    }

    private EventPricingResponse toEventPricingResponse(EventPricing pricing) {
        return new EventPricingResponse(
                pricing.getId(), pricing.getSection().getId(), pricing.getSection().getName(),
                pricing.getTicketType(), pricing.getPrice(), pricing.getCurrency(),
                pricing.getAvailableQuantity()
        );
    }
}
