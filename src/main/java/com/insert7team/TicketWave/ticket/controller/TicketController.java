package com.insert7team.TicketWave.ticket.controller;

import com.insert7team.TicketWave.shared.domain.dto.ApiResponse;
import com.insert7team.TicketWave.ticket.dto.DigitalTicketResponse;
import com.insert7team.TicketWave.ticket.dto.TicketResponse;
import com.insert7team.TicketWave.ticket.service.TicketService;
import com.insert7team.TicketWave.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    public TicketController(TicketService ticketService, UserService userService) {
        this.ticketService = ticketService;
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getUserTickets(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserEntityByEmail(userDetails.getUsername()).getId();
        List<TicketResponse> response = ticketService.getUserTickets(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicket(@PathVariable Long ticketId) {
        TicketResponse response = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/code/{ticketCode}")
    public ResponseEntity<ApiResponse<DigitalTicketResponse>> getDigitalTicket(@PathVariable String ticketCode) {
        DigitalTicketResponse response = ticketService.getDigitalTicket(ticketCode);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{ticketId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelTicket(@PathVariable Long ticketId,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userService.getUserEntityByEmail(userDetails.getUsername()).getId();
        ticketService.cancelTicket(ticketId, userId);
        return ResponseEntity.ok(ApiResponse.ok("Ticket cancelled", null));
    }

    @PostMapping("/validate/{ticketCode}")
    public ResponseEntity<ApiResponse<Void>> validateTicket(@PathVariable String ticketCode) {
        ticketService.markTicketUsed(ticketCode);
        return ResponseEntity.ok(ApiResponse.ok("Ticket validated", null));
    }
}
