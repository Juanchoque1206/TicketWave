package com.insert7team.TicketWave.user.controller;

import com.insert7team.TicketWave.shared.domain.dto.ApiResponse;
import com.insert7team.TicketWave.user.dto.UpdateProfileRequest;
import com.insert7team.TicketWave.user.dto.UserProfileResponse;
import com.insert7team.TicketWave.user.entity.User;
import com.insert7team.TicketWave.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getUserEntityByEmail(userDetails.getUsername());
        UserProfileResponse profile = userService.getProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UpdateProfileRequest request) {
        User user = userService.getUserEntityByEmail(userDetails.getUsername());
        UserProfileResponse profile = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }
}
