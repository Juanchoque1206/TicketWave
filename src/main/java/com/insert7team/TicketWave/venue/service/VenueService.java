package com.insert7team.TicketWave.venue.service;

import com.insert7team.TicketWave.shared.domain.dto.PagedResponse;
import com.insert7team.TicketWave.venue.dto.*;
import org.springframework.data.domain.Pageable;

public interface VenueService {
    VenueResponse createVenue(VenueRequest request);
    VenueResponse updateVenue(Long venueId, VenueRequest request);
    VenueResponse getVenue(Long venueId);
    PagedResponse<VenueResponse> getVenuesByCity(String city, Pageable pageable);
    SeatMapResponse getSeatMap(Long venueId);
    SectionResponse addSection(Long venueId, SectionRequest request);
    void generateSeats(Long sectionId, int rows, int seatsPerRow);
}
