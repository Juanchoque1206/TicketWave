package com.insert7team.TicketWave.user.service;

import com.insert7team.TicketWave.user.dto.LoginRequest;
import com.insert7team.TicketWave.user.dto.LoginResponse;
import com.insert7team.TicketWave.user.dto.RegisterRequest;

public interface AuthService {
    LoginResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
