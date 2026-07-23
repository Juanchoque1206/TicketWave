package com.insert7team.TicketWave.shared.infrastructure.exception;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) {
        super(message);
    }
}
