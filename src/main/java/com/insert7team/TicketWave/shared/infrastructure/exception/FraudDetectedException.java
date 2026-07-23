package com.insert7team.TicketWave.shared.infrastructure.exception;

public class FraudDetectedException extends RuntimeException {
    public FraudDetectedException(String message) {
        super(message);
    }
}
