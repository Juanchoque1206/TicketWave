package com.insert7team.TicketWave.common.kafka;

public final class KafkaTopics {
    public static final String ORDER_CREATED = "ticketwave.order.created";
    public static final String ORDER_COMPLETED = "ticketwave.order.completed";
    public static final String PAYMENT_COMPLETED = "ticketwave.payment.completed";
    public static final String REFUND_PROCESSED = "ticketwave.refund.processed";
    public static final String EVENT_CHANGED = "ticketwave.event.changed";
    public static final String EVENT_CANCELLED = "ticketwave.event.cancelled";
    public static final String NOTIFICATION_SEND = "ticketwave.notification.send";
    public static final String FRAUD_CHECK = "ticketwave.fraud.check";
    public static final String FRAUD_ALERT = "ticketwave.fraud.alert";

    private KafkaTopics() {}
}
