package com.insert7team.TicketWave.ticket.service;

import com.insert7team.TicketWave.ticket.dto.DigitalTicketResponse;
import com.insert7team.TicketWave.ticket.dto.TicketResponse;
import com.insert7team.TicketWave.ticket.entity.Ticket;
import com.insert7team.TicketWave.order.entity.Order;
import java.util.List;

public interface TicketService {
    TicketResponse getTicket(Long ticketId);
    DigitalTicketResponse getDigitalTicket(String ticketCode);
    List<TicketResponse> getUserTickets(Long userId);
    List<Ticket> issueTicketsForOrder(Order order);
    void cancelTicket(Long ticketId, Long userId);
    void markTicketUsed(String ticketCode);
}
