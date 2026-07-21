package com.insert7team.TicketWave.user.dto;

public record LoginResponse(
        String token,
        String email,
        String firstName,
        String role,
        Long expiresIn
) {}
