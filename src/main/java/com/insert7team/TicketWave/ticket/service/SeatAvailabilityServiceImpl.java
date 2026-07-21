package com.insert7team.TicketWave.ticket.service;

import com.insert7team.TicketWave.common.enums.SeatStatus;
import com.insert7team.TicketWave.event.entity.EventPricing;
import com.insert7team.TicketWave.event.repository.EventPricingRepository;
import com.insert7team.TicketWave.ticket.dto.SeatAvailabilityResponse;
import com.insert7team.TicketWave.ticket.dto.SeatStatusResponse;
import com.insert7team.TicketWave.venue.entity.Seat;
import com.insert7team.TicketWave.venue.entity.Section;
import com.insert7team.TicketWave.venue.repository.SeatRepository;
import com.insert7team.TicketWave.venue.repository.SectionRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class SeatAvailabilityServiceImpl implements SeatAvailabilityService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SeatRepository seatRepository;
    private final SectionRepository sectionRepository;
    private final EventPricingRepository eventPricingRepository;

    public SeatAvailabilityServiceImpl(RedisTemplate<String, String> redisTemplate,
                                       SeatRepository seatRepository,
                                       SectionRepository sectionRepository,
                                       EventPricingRepository eventPricingRepository) {
        this.redisTemplate = redisTemplate;
        this.seatRepository = seatRepository;
        this.sectionRepository = sectionRepository;
        this.eventPricingRepository = eventPricingRepository;
    }

    @Override
    public boolean reserveSeat(Long eventId, Long seatId, Long userId, Duration holdDuration) {
        String key = "seat:" + eventId + ":" + seatId;
        String value = "RESERVED:" + userId;
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, holdDuration);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public void releaseSeat(Long eventId, Long seatId) {
        String key = "seat:" + eventId + ":" + seatId;
        redisTemplate.delete(key);
    }

    @Override
    public void confirmSeat(Long eventId, Long seatId) {
        String key = "seat:" + eventId + ":" + seatId;
        redisTemplate.opsForValue().set(key, "SOLD");
    }

    @Override
    public SeatStatus getSeatStatus(Long eventId, Long seatId) {
        String key = "seat:" + eventId + ":" + seatId;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return SeatStatus.AVAILABLE;
        if (value.equals("SOLD")) return SeatStatus.SOLD;
        if (value.startsWith("RESERVED")) return SeatStatus.RESERVED;
        return SeatStatus.AVAILABLE;
    }

    @Override
    public SeatAvailabilityResponse getAvailability(Long eventId, Long sectionId) {
        Section section = sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));

        if (section.isGeneralAdmission()) {
            int available = getAvailableCountGA(eventId, sectionId);
            return new SeatAvailabilityResponse(eventId, sectionId, section.getName(),
                    true, section.getCapacity(), available, List.of());
        }

        List<Seat> seats = seatRepository.findBySectionId(sectionId);
        List<SeatStatusResponse> seatStatuses = seats.stream()
                .map(seat -> new SeatStatusResponse(
                        seat.getId(), seat.getRow(), seat.getNumber(),
                        seat.getLabel(), getSeatStatus(eventId, seat.getId())
                )).toList();

        long availableCount = seatStatuses.stream()
                .filter(s -> s.status() == SeatStatus.AVAILABLE)
                .count();

        return new SeatAvailabilityResponse(eventId, sectionId, section.getName(),
                false, section.getCapacity(), (int) availableCount, seatStatuses);
    }

    @Override
    public int getAvailableCountGA(Long eventId, Long sectionId) {
        String key = "ga:" + eventId + ":" + sectionId + ":avail";
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    @Override
    public boolean reserveGA(Long eventId, Long sectionId, int quantity, Long userId, Duration holdDuration) {
        String key = "ga:" + eventId + ":" + sectionId + ":avail";
        Long remaining = redisTemplate.opsForValue().decrement(key, quantity);
        if (remaining == null || remaining < 0) {
            redisTemplate.opsForValue().increment(key, quantity);
            return false;
        }
        return true;
    }

    @Override
    public void releaseGA(Long eventId, Long sectionId, int quantity) {
        String key = "ga:" + eventId + ":" + sectionId + ":avail";
        redisTemplate.opsForValue().increment(key, quantity);
    }

    @Override
    public void confirmGA(Long eventId, Long sectionId, int quantity) {
        // GA seats are already decremented during reservation; confirmation is a no-op
        // The count stays decremented permanently
    }

    @Override
    public void initializeEventAvailability(Long eventId) {
        List<EventPricing> pricings = eventPricingRepository.findByEventId(eventId);
        for (EventPricing pricing : pricings) {
            Section section = pricing.getSection();
            if (section.isGeneralAdmission()) {
                String key = "ga:" + eventId + ":" + section.getId() + ":avail";
                redisTemplate.opsForValue().set(key, String.valueOf(pricing.getAvailableQuantity()));
            }
            // For assigned seats, availability is determined by absence of Redis key (SETNX pattern)
        }
    }
}
