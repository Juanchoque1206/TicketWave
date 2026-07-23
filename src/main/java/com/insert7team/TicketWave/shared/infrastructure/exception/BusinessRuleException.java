package com.insert7team.TicketWave.shared.infrastructure.exception;

public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
