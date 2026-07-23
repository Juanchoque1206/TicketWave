package com.insert7team.TicketWave.order.entity;

import com.insert7team.TicketWave.shared.domain.model.AggregateRoot;
import com.insert7team.TicketWave.order.domain.OrderStatus;
import com.insert7team.TicketWave.shared.infrastructure.exception.BusinessRuleException;
import com.insert7team.TicketWave.shared.infrastructure.exception.UnauthorizedAccessException;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order extends AggregateRoot {

    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @Column(length = 50)
    private String promotionCode;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    // --- Domain behavior ---

    public static String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        return "TW-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void cancel() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessRuleException("Only pending orders can be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public void markPaymentProcessing() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessRuleException("Order is not in a payable state");
        }
        this.status = OrderStatus.PAYMENT_PROCESSING;
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    public void markRefunded() {
        this.status = OrderStatus.REFUNDED;
    }

    public void markPartiallyRefunded() {
        this.status = OrderStatus.PARTIALLY_REFUNDED;
    }

    public void expire() {
        if (this.status != OrderStatus.PENDING) {
            return;
        }
        this.status = OrderStatus.CANCELLED;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void assertOwnedBy(Long userId) {
        if (!this.userId.equals(userId)) {
            throw new UnauthorizedAccessException("You can only access your own orders");
        }
    }

    // --- Getters and setters ---

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getPromotionCode() { return promotionCode; }
    public void setPromotionCode(String promotionCode) { this.promotionCode = promotionCode; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
