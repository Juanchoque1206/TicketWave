package com.insert7team.TicketWave.payment.service;

import com.insert7team.TicketWave.common.enums.OrderStatus;
import com.insert7team.TicketWave.common.enums.PaymentStatus;
import com.insert7team.TicketWave.common.exception.BusinessRuleException;
import com.insert7team.TicketWave.common.exception.PaymentFailedException;
import com.insert7team.TicketWave.common.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.order.entity.Order;
import com.insert7team.TicketWave.order.repository.OrderRepository;
import com.insert7team.TicketWave.order.service.OrderService;
import com.insert7team.TicketWave.payment.dto.PaymentRequest;
import com.insert7team.TicketWave.payment.dto.PaymentResponse;
import com.insert7team.TicketWave.payment.dto.PaymentWebhookPayload;
import com.insert7team.TicketWave.payment.entity.Payment;
import com.insert7team.TicketWave.payment.kafka.PaymentCompletedEvent;
import com.insert7team.TicketWave.payment.kafka.PaymentKafkaProducer;
import com.insert7team.TicketWave.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentGateway paymentGateway;
    private final PaymentKafkaProducer paymentKafkaProducer;

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository,
                              OrderService orderService, PaymentGateway paymentGateway,
                              PaymentKafkaProducer paymentKafkaProducer) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.paymentGateway = paymentGateway;
        this.paymentKafkaProducer = paymentKafkaProducer;
    }

    @Override
    @Transactional
    public PaymentResponse initiatePayment(Long orderId, PaymentRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessRuleException("Order is not in a payable state");
        }
        if (LocalDateTime.now().isAfter(order.getExpiresAt())) {
            throw new BusinessRuleException("Order has expired");
        }

        order.setStatus(OrderStatus.PAYMENT_PROCESSING);
        orderRepository.save(order);

        String externalId = paymentGateway.processPayment(
                order.getTotalAmount(), order.getCurrency(), request.paymentMethod(),
                Map.of("orderId", String.valueOf(orderId), "orderNumber", order.getOrderNumber())
        );

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setExternalPaymentId(externalId);
        payment.setAmount(order.getTotalAmount());
        payment.setCurrency(order.getCurrency());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setStatus(PaymentStatus.PROCESSING);
        payment = paymentRepository.save(payment);

        // In mock mode, auto-complete the payment
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        orderService.completeOrder(orderId);

        paymentKafkaProducer.publishPaymentCompleted(new PaymentCompletedEvent(
                UUID.randomUUID().toString(), "PAYMENT_COMPLETED", LocalDateTime.now(),
                payment.getId(), orderId, payment.getAmount(), payment.getPaymentMethod()
        ));

        return toPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentStatus(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));
        return toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public void handlePaymentWebhook(PaymentWebhookPayload payload) {
        if (!paymentGateway.verifyWebhookSignature(payload.rawPayload(), payload.signature())) {
            throw new PaymentFailedException("Invalid webhook signature");
        }

        Payment payment = paymentRepository.findByExternalPaymentId(payload.externalPaymentId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if ("success".equalsIgnoreCase(payload.status())) {
            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setPaidAt(LocalDateTime.now());
            paymentRepository.save(payment);
            orderService.completeOrder(payment.getOrder().getId());

            paymentKafkaProducer.publishPaymentCompleted(new PaymentCompletedEvent(
                    UUID.randomUUID().toString(), "PAYMENT_COMPLETED", LocalDateTime.now(),
                    payment.getId(), payment.getOrder().getId(), payment.getAmount(), payment.getPaymentMethod()
            ));
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(payload.failureReason());
            paymentRepository.save(payment);
            orderService.cancelOrder(payment.getOrder().getId(), payment.getOrder().getUser().getId());
        }
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(), payment.getOrder().getId(), payment.getAmount(),
                payment.getCurrency(), payment.getPaymentMethod(), payment.getStatus(),
                payment.getExternalPaymentId(), payment.getPaidAt()
        );
    }
}
