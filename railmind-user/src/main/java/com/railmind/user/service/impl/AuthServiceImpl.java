package com.railmind.user.service.impl;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.common.util.CryptoUtil;
import com.railmind.user.domain.model.User;
import com.railmind.user.domain.service.UserDomainService;
import com.railmind.user.dto.*;
import com.railmind.user.mapper.UserMapper;
import com.railmind.user.service.AuthService;
import com.railmind.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserDomainService userDomainService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "user:refresh:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "user:token:blacklist:";

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        log.info("User registration: username={}, phone={}", request.getUsername(), request.getPhone());

        userDomainService.checkUsernameAvailable(request.getUsername());
        userDomainService.checkPhoneAvailable(request.getPhone());

        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .realName(request.getRealName())
                .createdAt(now)
                .updatedAt(now)
                .build();

        userMapper.insert(user);
        log.info("User registered successfully: userId={}", user.getId());

        return toUserDTO(user);
    }

    @Override
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

    @Override
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

    @Override
    public void logout(Long userId, String accessToken) {
        log.info("User logout: userId={}", userId);

        // Delete refresh token from Redis
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + userId);

        // Blacklist the access token (add to Redis with remaining TTL)
        if (accessToken != null) {
            try {
                long expiration = jwtUtil.getAccessTokenExpiration();
                redisTemplate.opsForValue().set(
                        ACCESS_TOKEN_BLACKLIST_PREFIX + accessToken,
                        "1",
                        expiration,
                        TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.warn("Failed to blacklist access token: {}", e.getMessage());
            }
        }

        log.info("User logout successful: userId={}", userId);
    }

    private UserDTO toUserDTO(User user) {
        String maskedIdCard = null;
        if (user.getIdCard() != null) {
            try {
                maskedIdCard = CryptoUtil.maskIdCard(CryptoUtil.aesDecrypt(user.getIdCard()));
            } catch (Exception e) {
                log.warn("Failed to decrypt idCard for user: {}", user.getId());
            }
        }

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .maskedIdCard(maskedIdCard)
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .userLevel(user.getUserLevel())
                .lastLoginTime(user.getLastLoginTime())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
