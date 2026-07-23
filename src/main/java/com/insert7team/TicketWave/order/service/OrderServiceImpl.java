package com.insert7team.TicketWave.order.service;

import com.insert7team.TicketWave.shared.domain.dto.PagedResponse;
import com.insert7team.TicketWave.order.domain.OrderStatus;
import com.insert7team.TicketWave.ticket.domain.TicketStatus;
import com.insert7team.TicketWave.shared.infrastructure.exception.BusinessRuleException;
import com.insert7team.TicketWave.shared.infrastructure.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.shared.infrastructure.exception.SeatUnavailableException;
import com.insert7team.TicketWave.event.entity.Event;
import com.insert7team.TicketWave.event.entity.EventPricing;
import com.insert7team.TicketWave.event.repository.EventPricingRepository;
import com.insert7team.TicketWave.event.repository.EventRepository;
import com.insert7team.TicketWave.order.dto.*;
import com.insert7team.TicketWave.order.entity.Order;
import com.insert7team.TicketWave.order.entity.OrderItem;
import com.insert7team.TicketWave.order.kafka.OrderCompletedEvent;
import com.insert7team.TicketWave.order.kafka.OrderCreatedEvent;
import com.insert7team.TicketWave.order.kafka.OrderKafkaProducer;
import com.insert7team.TicketWave.order.repository.OrderRepository;
import com.insert7team.TicketWave.ticket.entity.Ticket;
import com.insert7team.TicketWave.ticket.repository.TicketRepository;
import com.insert7team.TicketWave.ticket.service.SeatAvailabilityService;
import com.insert7team.TicketWave.ticket.service.TicketService;
import com.insert7team.TicketWave.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;
    private final EventPricingRepository eventPricingRepository;
    private final TicketRepository ticketRepository;
    private final SeatAvailabilityService seatAvailabilityService;
    private final TicketService ticketService;
    private final OrderKafkaProducer orderKafkaProducer;
    private final UserService userService;

    @Value("${ticketwave.order.expiration-minutes}")
    private int orderExpirationMinutes;

    public OrderServiceImpl(OrderRepository orderRepository, EventRepository eventRepository,
                            EventPricingRepository eventPricingRepository,
                            TicketRepository ticketRepository,
                            SeatAvailabilityService seatAvailabilityService,
                            TicketService ticketService, OrderKafkaProducer orderKafkaProducer,
                            UserService userService) {
        this.orderRepository = orderRepository;
        this.eventRepository = eventRepository;
        this.eventPricingRepository = eventPricingRepository;
        this.ticketRepository = ticketRepository;
        this.seatAvailabilityService = seatAvailabilityService;
        this.ticketService = ticketService;
        this.orderKafkaProducer = orderKafkaProducer;
        this.userService = userService;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (!event.isSalesActive()) {
            throw new BusinessRuleException("Ticket sales are not active for this event");
        }

        int totalQuantity = request.items().stream().mapToInt(OrderItemRequest::quantity).sum();
        int existingTickets = ticketRepository.countByEventIdAndUserIdAndStatusNot(
                event.getId(), userId, TicketStatus.CANCELLED);
        if (existingTickets + totalQuantity > event.getMaxTicketsPerUser()) {
            throw new BusinessRuleException("Maximum tickets per user exceeded. Limit: " + event.getMaxTicketsPerUser());
        }

        Duration holdDuration = Duration.ofMinutes(orderExpirationMinutes);
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.items()) {
            EventPricing pricing = eventPricingRepository
                    .findByEventIdAndSectionIdAndTicketType(event.getId(), itemReq.sectionId(), itemReq.ticketType())
                    .orElseThrow(() -> new ResourceNotFoundException("Pricing not found for this section and ticket type"));

            if (itemReq.seatId() != null) {
                boolean reserved = seatAvailabilityService.reserveSeat(
                        event.getId(), itemReq.seatId(), userId, holdDuration);
                if (!reserved) {
                    throw new SeatUnavailableException("Seat is not available");
                }
                OrderItem item = new OrderItem();
                item.setEventId(event.getId());
                item.setEventTitle(event.getTitle());
                item.setSectionId(itemReq.sectionId());
                item.setSectionName(pricing.getSectionName());
                item.setSeatId(itemReq.seatId());
                item.setTicketType(itemReq.ticketType());
                item.setUnitPrice(pricing.getPrice());
                item.setQuantity(1);
                orderItems.add(item);
                subtotal = subtotal.add(pricing.getPrice());
            } else {
                boolean reserved = seatAvailabilityService.reserveGA(
                        event.getId(), itemReq.sectionId(), itemReq.quantity(), userId, holdDuration);
                if (!reserved) {
                    throw new SeatUnavailableException("Not enough general admission tickets available");
                }
                OrderItem item = new OrderItem();
                item.setEventId(event.getId());
                item.setEventTitle(event.getTitle());
                item.setSectionId(itemReq.sectionId());
                item.setSectionName(pricing.getSectionName());
                item.setTicketType(itemReq.ticketType());
                item.setUnitPrice(pricing.getPrice());
                item.setQuantity(itemReq.quantity());
                orderItems.add(item);
                subtotal = subtotal.add(pricing.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())));
            }
        }

        Order order = new Order();
        order.setOrderNumber(Order.generateOrderNumber());
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);
        order.setCurrency("USD");
        order.setPromotionCode(request.promotionCode());
        order.setExpiresAt(LocalDateTime.now().plusMinutes(orderExpirationMinutes));

        order = orderRepository.save(order);
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);
        order = orderRepository.save(order);

        List<Long> eventIds = List.of(event.getId());
        orderKafkaProducer.publishOrderCreated(new OrderCreatedEvent(
                UUID.randomUUID().toString(), "ORDER_CREATED", LocalDateTime.now(),
                order.getId(), userId, order.getTotalAmount(), orderItems.size(), eventIds
        ));

        return toOrderResponse(order);
    }

    @Override
    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = getOrderEntity(orderId);
        order.assertOwnedBy(userId);
        return toOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderByNumber(String orderNumber, Long userId) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.assertOwnedBy(userId);
        return toOrderResponse(order);
    }

    @Override
    public PagedResponse<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        Page<OrderResponse> page = orderRepository.findByUserId(userId, pageable)
                .map(this::toOrderResponse);
        return PagedResponse.from(page);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getOrderEntity(orderId);
        order.assertOwnedBy(userId);
        order.cancel();
        orderRepository.save(order);
        releaseOrderSeats(order);
    }

    @Override
    @Transactional
    public void expireStaleOrders() {
        List<Order> staleOrders = orderRepository.findByStatusAndExpiresAtBefore(
                OrderStatus.PENDING, LocalDateTime.now());
        for (Order order : staleOrders) {
            order.expire();
            orderRepository.save(order);
            releaseOrderSeats(order);
            log.info("Expired stale order: {}", order.getOrderNumber());
        }
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        Order order = getOrderEntity(orderId);
        order.complete();
        orderRepository.save(order);

        List<Ticket> tickets = ticketService.issueTicketsForOrder(order);
        List<String> ticketCodes = tickets.stream().map(Ticket::getTicketCode).toList();

        String userEmail = userService.getUserEntityById(order.getUserId()).getEmail();
        orderKafkaProducer.publishOrderCompleted(new OrderCompletedEvent(
                UUID.randomUUID().toString(), "ORDER_COMPLETED", LocalDateTime.now(),
                order.getId(), order.getUserId(), userEmail, order.getOrderNumber(),
                order.getTotalAmount(), order.getCurrency(), ticketCodes
        ));
    }

    @Override
    @Transactional
    public void markPaymentProcessing(Long orderId) {
        Order order = getOrderEntity(orderId);
        order.markPaymentProcessing();
        orderRepository.save(order);
    }

    @Override
    public Order getOrderEntity(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    private void releaseOrderSeats(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getSeatId() != null) {
                seatAvailabilityService.releaseSeat(item.getEventId(), item.getSeatId());
            } else {
                seatAvailabilityService.releaseGA(item.getEventId(), item.getSectionId(), item.getQuantity());
            }
        }
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getEventId(),
                        item.getEventTitle(),
                        item.getSectionId(),
                        item.getSectionName(),
                        item.getSeatId(),
                        item.getSeatLabel(),
                        item.getTicketType(),
                        item.getUnitPrice(),
                        item.getQuantity()
                )).toList();

        return new OrderResponse(
                order.getId(), order.getOrderNumber(), order.getStatus(),
                order.getSubtotal(), order.getDiscountAmount(), order.getTotalAmount(),
                order.getCurrency(), order.getPromotionCode(), order.getExpiresAt(),
                items, order.getCreatedAt()
        );
    }
}
