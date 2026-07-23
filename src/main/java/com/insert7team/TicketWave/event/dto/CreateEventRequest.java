package com.insert7team.TicketWave.event.dto;

import com.insert7team.TicketWave.event.domain.EventCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank String title,
        String description,
        String artist,
        @NotNull EventCategory category,
        @NotNull @Future LocalDateTime eventDate,
        LocalDateTime doorsOpenAt,
        @NotNull Long venueId,
        int maxTicketsPerUser,
        @NotNull @Future LocalDateTime salesStartAt,
        @NotNull @Future LocalDateTime salesEndAt,
        String imageUrl
) {}
