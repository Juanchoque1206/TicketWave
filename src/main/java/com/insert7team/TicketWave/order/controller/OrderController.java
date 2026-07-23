package com.insert7team.TicketWave.order.controller;

import com.insert7team.TicketWave.shared.domain.dto.ApiResponse;
import com.insert7team.TicketWave.shared.domain.dto.PagedResponse;
import com.insert7team.TicketWave.order.dto.CreateOrderRequest;
import com.insert7team.TicketWave.order.dto.OrderResponse;
import com.insert7team.TicketWave.order.service.OrderService;
import com.insert7team.TicketWave.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {
        Long userId = userService.getUserEntityByEmail(userDetails.getUsername()).getId();
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Order created", response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserEntityByEmail(userDetails.getUsername()).getId();
        OrderResponse response = orderService.getOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserEntityByEmail(userDetails.getUsername()).getId();
        OrderResponse response = orderService.getOrderByNumber(orderNumber, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        Long userId = userService.getUserEntityByEmail(userDetails.getUsername()).getId();
        PagedResponse<OrderResponse> response = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserEntityByEmail(userDetails.getUsername()).getId();
        orderService.cancelOrder(orderId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled", null));
    }
}
