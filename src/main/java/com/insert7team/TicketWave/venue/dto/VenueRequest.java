package com.insert7team.TicketWave.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record VenueRequest(
        @NotBlank String name,
        @NotBlank String city,
        @NotBlank String address,
        @NotBlank String country,
        @Positive int totalCapacity,
        boolean hasAssignedSeating,
        String externalSeatMapId,
        String contactEmail
) {}
