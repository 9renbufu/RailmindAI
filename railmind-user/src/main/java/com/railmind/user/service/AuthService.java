package com.railmind.user.service;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.common.util.CryptoUtil;
import com.railmind.user.domain.model.User;
import com.railmind.user.domain.repository.UserRepository;
import com.railmind.user.domain.service.UserDomainService;
import com.railmind.user.dto.*;
import com.railmind.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "user:refresh:";

    @Transactional
    public UserDTO register(RegisterRequest request) {
        log.info("User registration: username={}, phone={}", request.getUsername(), request.getPhone());

        userDomainService.checkUsernameAvailable(request.getUsername());
        userDomainService.checkPhoneAvailable(request.getPhone());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .realName(request.getRealName())
                .build();

        user = userRepository.save(user);
        log.info("User registered successfully: userId={}", user.getId());

        return toUserDTO(user);
    }

    public TokenResponse login(LoginRequest request, String clientIp) {
        log.info("User login: username={}", request.getUsername());

        User user = userDomainService.getActiveUserByUsername(request.getUsername());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - wrong password: username={}", request.getUsername());
            throw new BizException(ErrorCode.USER_PASSWORD_ERROR);
        }

        if (user.getStatus() == 0) {
            throw new BizException(ErrorCode.USER_DISABLED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        redisTemplate.opsForValue().set(
                REFRESH_TOKEN_PREFIX + user.getId(),
                refreshToken,
                7, TimeUnit.DAYS);

        userDomainService.updateLoginInfo(user, clientIp);

        log.info("User login successful: userId={}", user.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .tokenType("Bearer")
                .build();
    }

    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }

        Long userId = jwtUtil.getUserId(refreshToken);
        String storedToken = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + userId);
        if (!refreshToken.equals(storedToken)) {
            throw new BizException(ErrorCode.TOKEN_INVALID);
        }

        User user = userDomainService.getActiveUser(userId);
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());

        log.info("Token refreshed: userId={}", userId);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                .tokenType("Bearer")
                .build();
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .maskedIdCard(user.getIdCard() != null ? CryptoUtil.maskIdCard(user.getIdCard()) : null)
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
