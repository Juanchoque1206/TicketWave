package com.insert7team.TicketWave.user.service;

import com.insert7team.TicketWave.user.dto.UpdateProfileRequest;
import com.insert7team.TicketWave.user.dto.UserProfileResponse;
import com.insert7team.TicketWave.user.entity.User;

public interface UserService {
    UserProfileResponse getProfile(Long userId);
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);
    User getUserEntityById(Long userId);
    User getUserEntityByEmail(String email);
}
