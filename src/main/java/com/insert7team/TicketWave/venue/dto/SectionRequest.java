package com.insert7team.TicketWave.venue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SectionRequest(
        @NotBlank String name,
        @Positive int capacity,
        boolean generalAdmission
) {}
