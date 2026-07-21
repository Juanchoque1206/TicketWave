package com.insert7team.TicketWave.fraud.service;

import com.insert7team.TicketWave.fraud.entity.FraudAlert;
import com.insert7team.TicketWave.order.dto.CreateOrderRequest;
import java.util.List;

public interface FraudDetectionService {
    int assessRisk(Long userId, CreateOrderRequest request);
    boolean shouldBlockOrder(Long userId, Long eventId);
    List<FraudAlert> getUnresolvedAlerts();
    void resolveAlert(Long alertId, String resolvedBy);
}
