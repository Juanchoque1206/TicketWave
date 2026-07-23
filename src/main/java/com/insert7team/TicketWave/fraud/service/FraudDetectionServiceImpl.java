package com.insert7team.TicketWave.fraud.service;

import com.insert7team.TicketWave.shared.infrastructure.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.fraud.entity.FraudAlert;
import com.insert7team.TicketWave.fraud.repository.FraudAlertRepository;
import com.insert7team.TicketWave.order.dto.CreateOrderRequest;
import com.insert7team.TicketWave.order.dto.OrderItemRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final FraudAlertRepository fraudAlertRepository;

    @Value("${ticketwave.fraud.max-orders-per-hour}")
    private int maxOrdersPerHour;

    @Value("${ticketwave.fraud.high-risk-threshold}")
    private int highRiskThreshold;

    @Value("${ticketwave.fraud.block-threshold}")
    private int blockThreshold;

    public FraudDetectionServiceImpl(FraudAlertRepository fraudAlertRepository) {
        this.fraudAlertRepository = fraudAlertRepository;
    }

    @Override
    public int assessRisk(Long userId, CreateOrderRequest request) {
        int riskScore = 0;

        // Velocity check: orders in last hour
        int recentOrders = fraudAlertRepository.countByUserIdAndCreatedAtAfter(
                userId, LocalDateTime.now().minusHours(1));
        if (recentOrders >= maxOrdersPerHour) {
            riskScore += 40;
        } else if (recentOrders >= maxOrdersPerHour / 2) {
            riskScore += 20;
        }

        // Quantity check: large quantity orders are suspicious
        int totalQuantity = request.items().stream().mapToInt(OrderItemRequest::quantity).sum();
        if (totalQuantity > 10) {
            riskScore += 30;
        } else if (totalQuantity > 5) {
            riskScore += 15;
        }

        // Multiple items targeting different sections could be resellers
        long distinctSections = request.items().stream()
                .map(OrderItemRequest::sectionId)
                .distinct()
                .count();
        if (distinctSections > 3) {
            riskScore += 20;
        }

        // Cap at 100
        riskScore = Math.min(riskScore, 100);

        if (riskScore >= highRiskThreshold) {
            FraudAlert alert = FraudAlert.create(userId, null, "AUTOMATED_RISK_ASSESSMENT",
                    riskScore, String.format("Velocity: %d orders/hr, Quantity: %d, Sections: %d",
                            recentOrders, totalQuantity, distinctSections));
            fraudAlertRepository.save(alert);
        }

        return riskScore;
    }

    @Override
    public boolean shouldBlockOrder(Long userId, Long eventId) {
        int recentAlerts = fraudAlertRepository.countByUserIdAndCreatedAtAfter(
                userId, LocalDateTime.now().minusHours(24));
        return recentAlerts >= 3;
    }

    @Override
    public List<FraudAlert> getUnresolvedAlerts() {
        return fraudAlertRepository.findByResolvedFalse();
    }

    @Override
    @Transactional
    public void resolveAlert(Long alertId, String resolvedBy) {
        FraudAlert alert = fraudAlertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Fraud alert not found"));
        alert.resolve(resolvedBy);
        fraudAlertRepository.save(alert);
    }
}
