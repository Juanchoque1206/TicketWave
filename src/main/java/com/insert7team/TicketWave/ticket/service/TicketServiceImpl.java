package com.insert7team.TicketWave.ticket.service;

import com.insert7team.TicketWave.shared.infrastructure.exception.ResourceNotFoundException;
import com.insert7team.TicketWave.shared.infrastructure.exception.UnauthorizedAccessException;
import com.insert7team.TicketWave.event.entity.Event;
import com.insert7team.TicketWave.event.repository.EventRepository;
import com.insert7team.TicketWave.order.entity.Order;
import com.insert7team.TicketWave.order.entity.OrderItem;
import com.insert7team.TicketWave.ticket.dto.DigitalTicketResponse;
import com.insert7team.TicketWave.ticket.dto.TicketResponse;
import com.insert7team.TicketWave.ticket.entity.Ticket;
import com.insert7team.TicketWave.ticket.repository.TicketRepository;
import com.insert7team.TicketWave.user.entity.User;
import com.insert7team.TicketWave.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final SeatAvailabilityService seatAvailabilityService;
    private final EventRepository eventRepository;
    private final UserService userService;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             SeatAvailabilityService seatAvailabilityService,
                             EventRepository eventRepository,
                             UserService userService) {
        this.ticketRepository = ticketRepository;
        this.seatAvailabilityService = seatAvailabilityService;
        this.eventRepository = eventRepository;
        this.userService = userService;
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
                ticket.getEventTitle(),
                ticket.getEventDate(),
                ticket.getVenueName(),
                ticket.getVenueAddress(),
                ticket.getSectionName(),
                ticket.getSeatLabel(),
                ticket.getHolderName(),
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
        Event event = eventRepository.findById(order.getItems().get(0).getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        User user = userService.getUserEntityById(order.getUserId());
        String holderName = user.getFirstName() + " " + user.getLastName();

        List<Ticket> tickets = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            for (int i = 0; i < item.getQuantity(); i++) {
                Ticket ticket = Ticket.issue(
                        item.getEventId(), order.getUserId(),
                        item.getSectionId(), item.getSeatId(),
                        item.getTicketType(), item.getUnitPrice(),
                        item.getEventTitle(), event.getEventDate(),
                        event.getVenueName(), null,
                        item.getSectionName(), item.getSeatLabel(),
                        holderName
                );

                if (item.getSeatId() != null) {
                    seatAvailabilityService.confirmSeat(item.getEventId(), item.getSeatId());
                } else {
                    seatAvailabilityService.confirmGA(item.getEventId(), item.getSectionId(), 1);
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
        if (!ticket.getUserId().equals(userId)) {
            throw new UnauthorizedAccessException("You can only cancel your own tickets");
        }
        ticket.cancel();
        ticketRepository.save(ticket);

        if (ticket.getSeatId() != null) {
            seatAvailabilityService.releaseSeat(ticket.getEventId(), ticket.getSeatId());
        } else {
            seatAvailabilityService.releaseGA(ticket.getEventId(), ticket.getSectionId(), 1);
        }
    }

    @Override
    @Transactional
    public void markTicketUsed(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with code: " + ticketCode));
        ticket.markUsed();
        ticketRepository.save(ticket);
    }

    private TicketResponse toTicketResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getEventId(),
                ticket.getEventTitle(),
                ticket.getEventDate(),
                ticket.getVenueName(),
                ticket.getSectionName(),
                ticket.getSeatLabel(),
                ticket.getTicketType(),
                ticket.getStatus(),
                ticket.getPrice(),
                ticket.getCreatedAt()
        );
    }
}
