package com.insert7team.TicketWave.order.controller;

import com.insert7team.TicketWave.common.dto.ApiResponse;
import com.insert7team.TicketWave.common.dto.PagedResponse;
import com.insert7team.TicketWave.order.dto.CreateOrderRequest;
import com.insert7team.TicketWave.order.dto.OrderResponse;
import com.insert7team.TicketWave.order.service.OrderService;
import com.insert7team.TicketWave.user.entity.User;
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
        User user = userService.getUserEntityByEmail(userDetails.getUsername());
        OrderResponse response = orderService.createOrder(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Order created", response));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserEntityByEmail(userDetails.getUsername());
        OrderResponse response = orderService.getOrder(orderId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserEntityByEmail(userDetails.getUsername());
        OrderResponse response = orderService.getOrderByNumber(orderNumber, user.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        User user = userService.getUserEntityByEmail(userDetails.getUsername());
        PagedResponse<OrderResponse> response = orderService.getUserOrders(user.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserEntityByEmail(userDetails.getUsername());
        orderService.cancelOrder(orderId, user.getId());
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled", null));
    }
}
