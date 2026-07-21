package com.insert7team.TicketWave.order.service;

import com.insert7team.TicketWave.common.dto.PagedResponse;
import com.insert7team.TicketWave.common.enums.EventStatus;
import com.insert7team.TicketWave.common.enums.OrderStatus;
import com.insert7team.TicketWave.common.enums.TicketStatus;
import com.insert7team.TicketWave.common.exception.BusinessRuleException;
import com.insert7team.TicketWave.common.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.common.exception.SeatUnavailableException;
import com.insert7team.TicketWave.common.exception.UnauthorizedAccessException;
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
import com.insert7team.TicketWave.user.entity.User;
import com.insert7team.TicketWave.user.service.UserService;
import com.insert7team.TicketWave.venue.entity.Seat;
import com.insert7team.TicketWave.venue.entity.Section;
import com.insert7team.TicketWave.venue.repository.SeatRepository;
import com.insert7team.TicketWave.venue.repository.SectionRepository;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;
    private final EventPricingRepository eventPricingRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final UserService userService;
    private final SeatAvailabilityService seatAvailabilityService;
    private final TicketService ticketService;
    private final OrderKafkaProducer orderKafkaProducer;

    @Value("${ticketwave.order.expiration-minutes}")
    private int orderExpirationMinutes;

    public OrderServiceImpl(OrderRepository orderRepository, EventRepository eventRepository,
                            EventPricingRepository eventPricingRepository, SectionRepository sectionRepository,
                            SeatRepository seatRepository, TicketRepository ticketRepository,
                            UserService userService, SeatAvailabilityService seatAvailabilityService,
                            TicketService ticketService, OrderKafkaProducer orderKafkaProducer) {
        this.orderRepository = orderRepository;
        this.eventRepository = eventRepository;
        this.eventPricingRepository = eventPricingRepository;
        this.sectionRepository = sectionRepository;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.userService = userService;
        this.seatAvailabilityService = seatAvailabilityService;
        this.ticketService = ticketService;
        this.orderKafkaProducer = orderKafkaProducer;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        User user = userService.getUserEntityById(userId);
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        if (event.getStatus() != EventStatus.ON_SALE) {
            throw new BusinessRuleException("Event is not currently on sale");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(event.getSalesStartAt()) || now.isAfter(event.getSalesEndAt())) {
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
            Section section = sectionRepository.findById(itemReq.sectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

            EventPricing pricing = eventPricingRepository
                    .findByEventIdAndSectionIdAndTicketType(event.getId(), section.getId(), itemReq.ticketType())
                    .orElseThrow(() -> new ResourceNotFoundException("Pricing not found for this section and ticket type"));

            if (itemReq.seatId() != null) {
                Seat seat = seatRepository.findById(itemReq.seatId())
                        .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
                boolean reserved = seatAvailabilityService.reserveSeat(
                        event.getId(), seat.getId(), userId, holdDuration);
                if (!reserved) {
                    throw new SeatUnavailableException("Seat " + seat.getLabel() + " is not available");
                }
                OrderItem item = new OrderItem();
                item.setEvent(event);
                item.setSection(section);
                item.setSeat(seat);
                item.setTicketType(itemReq.ticketType());
                item.setUnitPrice(pricing.getPrice());
                item.setQuantity(1);
                orderItems.add(item);
                subtotal = subtotal.add(pricing.getPrice());
            } else {
                boolean reserved = seatAvailabilityService.reserveGA(
                        event.getId(), section.getId(), itemReq.quantity(), userId, holdDuration);
                if (!reserved) {
                    throw new SeatUnavailableException("Not enough general admission tickets available");
                }
                OrderItem item = new OrderItem();
                item.setEvent(event);
                item.setSection(section);
                item.setTicketType(itemReq.ticketType());
                item.setUnitPrice(pricing.getPrice());
                item.setQuantity(itemReq.quantity());
                orderItems.add(item);
                subtotal = subtotal.add(pricing.getPrice().multiply(BigDecimal.valueOf(itemReq.quantity())));
            }
        }

        String orderNumber = "TW-" + now.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setSubtotal(subtotal);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);
        order.setCurrency("USD");
        order.setPromotionCode(request.promotionCode());
        order.setExpiresAt(now.plusMinutes(orderExpirationMinutes));

        order = orderRepository.save(order);
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        order.setItems(orderItems);
        order = orderRepository.save(order);

        List<Long> eventIds = List.of(event.getId());
        orderKafkaProducer.publishOrderCreated(new OrderCreatedEvent(
                UUID.randomUUID().toString(), "ORDER_CREATED", now,
                order.getId(), userId, order.getTotalAmount(), orderItems.size(), eventIds
        ));

        return toOrderResponse(order);
    }

    @Override
    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only view your own orders");
        }
        return toOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderByNumber(String orderNumber, Long userId) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only view your own orders");
        }
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
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only cancel your own orders");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessRuleException("Only pending orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        releaseOrderSeats(order);
    }

    @Override
    @Transactional
    public void expireStaleOrders() {
        List<Order> staleOrders = orderRepository.findByStatusAndExpiresAtBefore(
                OrderStatus.PENDING, LocalDateTime.now());
        for (Order order : staleOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            releaseOrderSeats(order);
            log.info("Expired stale order: {}", order.getOrderNumber());
        }
    }

    @Override
    @Transactional
    public void completeOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);

        List<Ticket> tickets = ticketService.issueTicketsForOrder(order);
        List<String> ticketCodes = tickets.stream().map(Ticket::getTicketCode).toList();

        orderKafkaProducer.publishOrderCompleted(new OrderCompletedEvent(
                UUID.randomUUID().toString(), "ORDER_COMPLETED", LocalDateTime.now(),
                order.getId(), order.getUser().getId(), order.getOrderNumber(), ticketCodes
        ));
    }

    private void releaseOrderSeats(Order order) {
        for (OrderItem item : order.getItems()) {
            if (item.getSeat() != null) {
                seatAvailabilityService.releaseSeat(item.getEvent().getId(), item.getSeat().getId());
            } else {
                seatAvailabilityService.releaseGA(item.getEvent().getId(), item.getSection().getId(), item.getQuantity());
            }
        }
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getEvent().getId(),
                        item.getEvent().getTitle(),
                        item.getSection().getId(),
                        item.getSection().getName(),
                        item.getSeat() != null ? item.getSeat().getId() : null,
                        item.getSeat() != null ? item.getSeat().getLabel() : null,
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
