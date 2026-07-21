package com.insert7team.TicketWave.ticket.service;

import com.insert7team.TicketWave.common.enums.TicketStatus;
import com.insert7team.TicketWave.common.exception.BusinessRuleException;
import com.insert7team.TicketWave.common.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.common.exception.UnauthorizedAccessException;
import com.insert7team.TicketWave.order.entity.Order;
import com.insert7team.TicketWave.order.entity.OrderItem;
import com.insert7team.TicketWave.ticket.dto.DigitalTicketResponse;
import com.insert7team.TicketWave.ticket.dto.TicketResponse;
import com.insert7team.TicketWave.ticket.entity.Ticket;
import com.insert7team.TicketWave.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final SeatAvailabilityService seatAvailabilityService;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             SeatAvailabilityService seatAvailabilityService) {
        this.ticketRepository = ticketRepository;
        this.seatAvailabilityService = seatAvailabilityService;
    }

    @Override
    public TicketResponse getTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));
        return toTicketResponse(ticket);
    }

    @Override
    public DigitalTicketResponse getDigitalTicket(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with code: " + ticketCode));
        return new DigitalTicketResponse(
                ticket.getTicketCode(),
                ticket.getQrCodeData(),
                ticket.getEvent().getTitle(),
                ticket.getEvent().getEventDate(),
                ticket.getEvent().getVenue().getName(),
                ticket.getEvent().getVenue().getAddress(),
                ticket.getSection().getName(),
                ticket.getSeat() != null ? ticket.getSeat().getLabel() : null,
                ticket.getUser().getFirstName() + " " + ticket.getUser().getLastName(),
                ticket.getStatus()
        );
    }

    @Override
    public List<TicketResponse> getUserTickets(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(this::toTicketResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<Ticket> issueTicketsForOrder(Order order) {
        List<Ticket> tickets = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                Ticket ticket = new Ticket();
                ticket.setTicketCode(UUID.randomUUID().toString());
                ticket.setEvent(item.getEvent());
                ticket.setUser(order.getUser());
                ticket.setSeat(item.getSeat());
                ticket.setSection(item.getSection());
                ticket.setTicketType(item.getTicketType());
                ticket.setStatus(TicketStatus.CONFIRMED);
                ticket.setPrice(item.getUnitPrice());
                ticket.setQrCodeData("TW:" + ticket.getTicketCode());

                if (item.getSeat() != null) {
                    seatAvailabilityService.confirmSeat(item.getEvent().getId(), item.getSeat().getId());
                } else {
                    seatAvailabilityService.confirmGA(item.getEvent().getId(), item.getSection().getId(), 1);
                }

                tickets.add(ticketRepository.save(ticket));
            }
        }
        return tickets;
    }

    @Override
    @Transactional
    public void cancelTicket(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + ticketId));
        if (!ticket.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only cancel your own tickets");
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED || ticket.getStatus() == TicketStatus.USED) {
            throw new BusinessRuleException("Ticket cannot be cancelled in its current status");
        }
        ticket.setStatus(TicketStatus.CANCELLED);
        ticketRepository.save(ticket);

        if (ticket.getSeat() != null) {
            seatAvailabilityService.releaseSeat(ticket.getEvent().getId(), ticket.getSeat().getId());
        } else {
            seatAvailabilityService.releaseGA(ticket.getEvent().getId(), ticket.getSection().getId(), 1);
        }
    }

    @Override
    @Transactional
    public void markTicketUsed(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with code: " + ticketCode));
        if (ticket.getStatus() != TicketStatus.CONFIRMED) {
            throw new BusinessRuleException("Ticket is not in a valid state for entry");
        }
        ticket.setStatus(TicketStatus.USED);
        ticketRepository.save(ticket);
    }

    private TicketResponse toTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getEvent().getId(),
                ticket.getEvent().getTitle(),
                ticket.getEvent().getEventDate(),
                ticket.getEvent().getVenue().getName(),
                ticket.getSection().getName(),
                ticket.getSeat() != null ? ticket.getSeat().getLabel() : null,
                ticket.getTicketType(),
                ticket.getStatus(),
                ticket.getPrice(),
                ticket.getCreatedAt()
        );
    }
}
