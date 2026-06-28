package com.railmind.user.controller;

import com.railmind.common.model.Result;
import com.railmind.user.dto.*;
import com.railmind.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "注册/登录/Token刷新")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "手机号+密码注册")
    public Result<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = authService.register(request);
        return Result.ok(user);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "返回AccessToken+RefreshToken")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest httpRequest) {
        String clientIp = getClientIp(httpRequest);
        TokenResponse token = authService.login(request, clientIp);
        return Result.ok(token);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "RefreshToken换新AccessToken")
    public Result<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse token = authService.refreshToken(request.getRefreshToken());
        return Result.ok(token);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "清除Token，注销登录")
    public Result<Void> logout(Authentication authentication,
                               HttpServletRequest httpRequest) {
        Long userId = (Long) authentication.getPrincipal();
        String accessToken = httpRequest.getHeader("Authorization");
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        authService.logout(userId, accessToken);
        return Result.ok();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
