package com.insert7team.TicketWave.payment.entity;

import com.insert7team.TicketWave.shared.infrastructure.persistence.BaseEntity;
import com.insert7team.TicketWave.payment.domain.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
public class Refund extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(length = 200)
    private String externalRefundId;

    private LocalDateTime processedAt;

    // --- Domain behavior ---

    public void markProcessed(String externalRefundId) {
        this.status = PaymentStatus.COMPLETED;
        this.externalRefundId = externalRefundId;
        this.processedAt = LocalDateTime.now();
    }

    // --- Getters and setters ---

    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }
    public String getExternalRefundId() { return externalRefundId; }
    public void setExternalRefundId(String externalRefundId) { this.externalRefundId = externalRefundId; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
