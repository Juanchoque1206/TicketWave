package com.insert7team.TicketWave.order.service;

import com.insert7team.TicketWave.common.dto.PagedResponse;
import com.insert7team.TicketWave.order.dto.CreateOrderRequest;
import com.insert7team.TicketWave.order.dto.OrderResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponse createOrder(Long userId, CreateOrderRequest request);
    OrderResponse getOrder(Long orderId, Long userId);
    OrderResponse getOrderByNumber(String orderNumber, Long userId);
    PagedResponse<OrderResponse> getUserOrders(Long userId, Pageable pageable);
    void cancelOrder(Long orderId, Long userId);
    void expireStaleOrders();
    void completeOrder(Long orderId);
}
