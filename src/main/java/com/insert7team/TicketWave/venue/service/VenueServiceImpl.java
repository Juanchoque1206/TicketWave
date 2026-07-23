package com.insert7team.TicketWave.venue.service;

import com.insert7team.TicketWave.shared.domain.dto.PagedResponse;
import com.insert7team.TicketWave.shared.infrastructure.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.venue.dto.*;
import com.insert7team.TicketWave.venue.entity.Seat;
import com.insert7team.TicketWave.venue.entity.Section;
import com.insert7team.TicketWave.venue.entity.Venue;
import com.insert7team.TicketWave.venue.repository.SeatRepository;
import com.insert7team.TicketWave.venue.repository.SectionRepository;
import com.insert7team.TicketWave.venue.repository.VenueRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;

    public VenueServiceImpl(VenueRepository venueRepository, SectionRepository sectionRepository,
                            SeatRepository seatRepository) {
        this.venueRepository = venueRepository;
        this.sectionRepository = sectionRepository;
        this.seatRepository = seatRepository;
    }

    @Override
    @Transactional
    public VenueResponse createVenue(VenueRequest request) {
        Venue venue = new Venue();
        mapRequestToVenue(request, venue);
        venue = venueRepository.save(venue);
        return toVenueResponse(venue);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"venueDetail", "seatMap"}, key = "#venueId")
    public VenueResponse updateVenue(Long venueId, VenueRequest request) {
        Venue venue = getVenueEntity(venueId);
        mapRequestToVenue(request, venue);
        venue = venueRepository.save(venue);
        return toVenueResponse(venue);
    }

    @Override
    @Cacheable(value = "venueDetail", key = "#venueId")
    public VenueResponse getVenue(Long venueId) {
        Venue venue = getVenueEntity(venueId);
        return toVenueResponse(venue);
    }

    @Override
    public PagedResponse<VenueResponse> getVenuesByCity(String city, Pageable pageable) {
        Page<VenueResponse> page = venueRepository.findByCityIgnoreCase(city, pageable)
                .map(this::toVenueResponse);
        return PagedResponse.from(page);
    }

    @Override
    @Cacheable(value = "seatMap", key = "#venueId")
    public SeatMapResponse getSeatMap(Long venueId) {
        Venue venue = getVenueEntity(venueId);
        List<SectionWithSeatsResponse> sectionResponses = venue.getSections().stream()
                .map(section -> {
                    List<SeatResponse> seats = section.isGeneralAdmission()
                            ? List.of()
                            : section.getSeats().stream()
                                .map(seat -> new SeatResponse(seat.getId(), seat.getRow(), seat.getNumber(), seat.getLabel()))
                                .toList();
                    return new SectionWithSeatsResponse(
                            section.getId(), section.getName(), section.isGeneralAdmission(),
                            section.getCapacity(), seats
                    );
                }).toList();
        return new SeatMapResponse(venue.getId(), venue.getName(), venue.isHasAssignedSeating(), sectionResponses);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"venueDetail", "seatMap"}, allEntries = true)
    public SectionResponse addSection(Long venueId, SectionRequest request) {
        Venue venue = getVenueEntity(venueId);
        Section section = new Section();
        section.setName(request.name());
        section.setCapacity(request.capacity());
        section.setGeneralAdmission(request.generalAdmission());
        section.setVenue(venue);
        section = sectionRepository.save(section);
        return new SectionResponse(section.getId(), section.getName(), section.getCapacity(),
                section.isGeneralAdmission(), 0);
    }

    @Override
    @Transactional
    @CacheEvict(value = "seatMap", allEntries = true)
    public void generateSeats(Long sectionId, int rows, int seatsPerRow) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found with id: " + sectionId));

        List<Seat> seats = new ArrayList<>();
        for (int r = 1; r <= rows; r++) {
            String rowLabel = String.valueOf((char) ('A' + r - 1));
            for (int s = 1; s <= seatsPerRow; s++) {
                Seat seat = new Seat();
                seat.setRow(rowLabel);
                seat.setNumber(s);
                seat.setLabel(rowLabel + "-" + s);
                seat.setSection(section);
                seats.add(seat);
            }
        }
        seatRepository.saveAll(seats);
    }

    private Venue getVenueEntity(Long venueId) {
        return venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + venueId));
    }

    private void mapRequestToVenue(VenueRequest request, Venue venue) {
        venue.setName(request.name());
        venue.setCity(request.city());
        venue.setAddress(request.address());
        venue.setCountry(request.country());
        venue.setTotalCapacity(request.totalCapacity());
        venue.setHasAssignedSeating(request.hasAssignedSeating());
        venue.setExternalSeatMapId(request.externalSeatMapId());
        venue.setContactEmail(request.contactEmail());
    }

    private VenueResponse toVenueResponse(Venue venue) {
        return new VenueResponse(
                venue.getId(), venue.getName(), venue.getCity(), venue.getAddress(),
                venue.getCountry(), venue.getTotalCapacity(), venue.isHasAssignedSeating(),
                venue.getSections().size(), venue.getCreatedAt()
        );
    }
}
