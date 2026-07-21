package com.insert7team.TicketWave.payment.service;

import com.insert7team.TicketWave.common.enums.OrderStatus;
import com.insert7team.TicketWave.common.enums.PaymentStatus;
import com.insert7team.TicketWave.common.exception.BusinessRuleException;
import com.insert7team.TicketWave.common.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.order.entity.Order;
import com.insert7team.TicketWave.order.repository.OrderRepository;
import com.insert7team.TicketWave.payment.dto.RefundRequest;
import com.insert7team.TicketWave.payment.dto.RefundResponse;
import com.insert7team.TicketWave.payment.entity.Payment;
import com.insert7team.TicketWave.payment.entity.Refund;
import com.insert7team.TicketWave.payment.kafka.PaymentKafkaProducer;
import com.insert7team.TicketWave.payment.kafka.RefundProcessedEvent;
import com.insert7team.TicketWave.payment.repository.PaymentRepository;
import com.insert7team.TicketWave.payment.repository.RefundRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentKafkaProducer paymentKafkaProducer;

    public RefundServiceImpl(RefundRepository refundRepository, PaymentRepository paymentRepository,
                             OrderRepository orderRepository, PaymentGateway paymentGateway,
                             PaymentKafkaProducer paymentKafkaProducer) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.paymentGateway = paymentGateway;
        this.paymentKafkaProducer = paymentKafkaProducer;
    }

    @Override
    @Transactional
    public RefundResponse requestRefund(Long orderId, RefundRequest request) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessRuleException("Can only refund completed payments");
        }

        if (request.amount().compareTo(payment.getAmount()) > 0) {
            throw new BusinessRuleException("Refund amount exceeds payment amount");
        }

        String externalRefundId = paymentGateway.processRefund(payment.getExternalPaymentId(), request.amount());

        Refund refund = new Refund();
        refund.setPayment(payment);
        refund.setAmount(request.amount());
        refund.setReason(request.reason());
        refund.setStatus(PaymentStatus.COMPLETED);
        refund.setExternalRefundId(externalRefundId);
        refund.setProcessedAt(LocalDateTime.now());
        refund = refundRepository.save(refund);

        Order order = payment.getOrder();
        if (request.amount().compareTo(payment.getAmount()) == 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            order.setStatus(OrderStatus.REFUNDED);
        } else {
            order.setStatus(OrderStatus.PARTIALLY_REFUNDED);
        }
        orderRepository.save(order);

        paymentKafkaProducer.publishRefundProcessed(new RefundProcessedEvent(
                UUID.randomUUID().toString(), "REFUND_PROCESSED", LocalDateTime.now(),
                refund.getId(), orderId, order.getUser().getId(), refund.getAmount(), refund.getReason()
        ));

        return toRefundResponse(refund, orderId);
    }

    @Override
    public RefundResponse getRefundStatus(Long refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new ResourceNotFoundException("Refund not found"));
        return toRefundResponse(refund, refund.getPayment().getOrder().getId());
    }

    @Override
    public List<RefundResponse> getRefundsForOrder(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order"));
        return refundRepository.findByPaymentId(payment.getId()).stream()
                .map(r -> toRefundResponse(r, orderId))
                .toList();
    }

    private RefundResponse toRefundResponse(Refund refund, Long orderId) {
        return new RefundResponse(
                refund.getId(), orderId, refund.getAmount(), refund.getReason(),
                refund.getStatus(), refund.getProcessedAt()
        );
    }
}
