package com.railmind.user.service;

import com.railmind.common.exception.BizException;
import com.railmind.user.domain.model.User;
import com.railmind.user.domain.service.UserDomainService;
import com.railmind.user.dto.LoginRequest;
import com.railmind.user.dto.RegisterRequest;
import com.railmind.user.dto.TokenResponse;
import com.railmind.user.dto.UserDTO;
import com.railmind.user.mapper.UserMapper;
import com.railmind.user.service.impl.AuthServiceImpl;
import com.railmind.user.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserDomainService userDomainService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("$2a$10$encodedPassword")
                .phone("13800001111")
                .realName("张三")
                .status(1)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_shouldSucceed() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("123456");
        request.setPhone("13900009999");
        request.setRealName("新用户");

        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$encoded");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        UserDTO result = authService.register(request);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void register_shouldFailWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");
        request.setPhone("13900009999");

        doThrow(new BizException(1016, "用户名已存在"))
                .when(userDomainService).checkUsernameAvailable("testuser");

        assertThrows(BizException.class, () -> authService.register(request));
    }

    @Test
    void login_shouldReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        when(userDomainService.getActiveUserByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("123456", "$2a$10$encodedPassword")).thenReturn(true);
        when(jwtUtil.generateAccessToken(1L, "testuser")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(1L, "testuser")).thenReturn("refresh-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(900000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        TokenResponse result = authService.login(request, "127.0.0.1");

        assertNotNull(result);
        assertEquals("access-token", result.getAccessToken());
        assertEquals("refresh-token", result.getRefreshToken());
    }

    @Test
    void login_shouldFailWithWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(userDomainService.getActiveUserByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches("wrongpassword", "$2a$10$encodedPassword")).thenReturn(false);

        assertThrows(BizException.class, () -> authService.login(request, "127.0.0.1"));
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken() {
        String refreshToken = "valid-refresh-token";

        when(jwtUtil.isTokenValid(refreshToken)).thenReturn(true);
        when(jwtUtil.isRefreshToken(refreshToken)).thenReturn(true);
        when(jwtUtil.getUserId(refreshToken)).thenReturn(1L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:refresh:1")).thenReturn(refreshToken);
        when(userDomainService.getActiveUser(1L)).thenReturn(testUser);
        when(jwtUtil.generateAccessToken(1L, "testuser")).thenReturn("new-access-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(900000L);

        TokenResponse result = authService.refreshToken(refreshToken);

        assertNotNull(result);
        assertEquals("new-access-token", result.getAccessToken());
    }

    @Test
    void refreshToken_shouldFailWithInvalidToken() {
        when(jwtUtil.isTokenValid("invalid")).thenReturn(false);

        assertThrows(BizException.class, () -> authService.refreshToken("invalid"));
    }
}
