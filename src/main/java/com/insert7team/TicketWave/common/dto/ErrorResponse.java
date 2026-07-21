package com.insert7team.TicketWave.common.dto;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String message, LocalDateTime timestamp) {}
