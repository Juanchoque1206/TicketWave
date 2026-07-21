package com.insert7team.TicketWave.fraud.entity;

import com.insert7team.TicketWave.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlert extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    private Long orderId;

    @Column(nullable = false, length = 100)
    private String alertType;

    @Column(nullable = false)
    private int riskScore;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private boolean resolved = false;

    @Column(length = 200)
    private String resolvedBy;

    private LocalDateTime resolvedAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
    public String getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(String resolvedBy) { this.resolvedBy = resolvedBy; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
