package com.insert7team.TicketWave.user.dto;

public record UpdateProfileRequest(
        String firstName,
        String lastName,
        String phone
) {}
