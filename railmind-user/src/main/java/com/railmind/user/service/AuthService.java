package com.railmind.user.service;

import com.railmind.user.dto.*;

public interface AuthService {

    UserDTO register(RegisterRequest request);

    TokenResponse login(LoginRequest request, String clientIp);

    TokenResponse refreshToken(String refreshToken);

    void logout(Long userId, String accessToken);
}
