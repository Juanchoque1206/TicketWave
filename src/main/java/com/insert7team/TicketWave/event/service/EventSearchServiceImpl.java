package com.insert7team.TicketWave.event.service;

import com.insert7team.TicketWave.common.dto.PagedResponse;
import com.insert7team.TicketWave.common.enums.EventStatus;
import com.insert7team.TicketWave.event.dto.EventSearchCriteria;
import com.insert7team.TicketWave.event.dto.EventSummaryResponse;
import com.insert7team.TicketWave.event.entity.Event;
import com.insert7team.TicketWave.event.entity.EventPricing;
import com.insert7team.TicketWave.event.repository.EventRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class EventSearchServiceImpl implements EventSearchService {

    private final EventRepository eventRepository;

    public EventSearchServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Cacheable(value = "eventSearch", key = "#criteria.toCacheKey() + '_' + #pageable.pageNumber")
    public PagedResponse<EventSummaryResponse> searchEvents(EventSearchCriteria criteria, Pageable pageable) {
        Page<EventSummaryResponse> page = eventRepository.searchEvents(
                EventStatus.ON_SALE,
                criteria.city(),
                criteria.artist(),
                criteria.venueId(),
                criteria.dateFrom(),
                criteria.dateTo(),
                criteria.category(),
                pageable
        ).map(this::toSummaryResponse);
        return PagedResponse.from(page);
    }

    @Override
    @Cacheable(value = "upcomingEvents", key = "'upcoming_' + #limit")
    public List<EventSummaryResponse> getUpcomingEvents(int limit) {
        return eventRepository.findTop10ByStatusAndEventDateAfterOrderByEventDateAsc(
                EventStatus.ON_SALE, LocalDateTime.now()
        ).stream()
                .limit(limit)
                .map(this::toSummaryResponse)
                .toList();
    }

    private EventSummaryResponse toSummaryResponse(Event event) {
        BigDecimal minPrice = event.getPricings().stream()
                .map(EventPricing::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        return new EventSummaryResponse(
                event.getId(), event.getTitle(), event.getArtist(), event.getCategory(),
                event.getEventDate(), event.getStatus(), event.getVenue().getName(),
                event.getVenue().getCity(), minPrice, event.getImageUrl()
        );
    }
}
