package com.insert7team.TicketWave.event.service;

import com.insert7team.TicketWave.common.dto.PagedResponse;
import com.insert7team.TicketWave.event.dto.EventSearchCriteria;
import com.insert7team.TicketWave.event.dto.EventSummaryResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface EventSearchService {
    PagedResponse<EventSummaryResponse> searchEvents(EventSearchCriteria criteria, Pageable pageable);
    List<EventSummaryResponse> getUpcomingEvents(int limit);
}
