package com.railmind.user.controller;

import com.railmind.common.model.Result;
import com.railmind.user.dto.UpdateUserRequest;
import com.railmind.user.dto.UserDTO;
import com.railmind.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户信息", description = "获取/修改用户信息")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    public Result<UserDTO> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserDTO user = userService.getUserProfile(userId);
        return Result.ok(user);
    }

    @PutMapping("/profile")
    @Operation(summary = "修改用户信息", description = "修改头像/邮箱等")
    public Result<UserDTO> updateProfile(Authentication authentication,
                                         @Valid @RequestBody UpdateUserRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        UserDTO user = userService.updateUserProfile(userId, request);
        return Result.ok(user);
    }
}
