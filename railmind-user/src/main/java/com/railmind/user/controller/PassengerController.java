package com.railmind.user.controller;

import com.railmind.common.model.Result;
import com.railmind.user.dto.AddPassengerRequest;
import com.railmind.user.dto.PassengerDTO;
import com.railmind.user.dto.UpdatePassengerRequest;
import com.railmind.user.service.PassengerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/passenger")
@RequiredArgsConstructor
@Tag(name = "乘车人管理", description = "乘车人CRUD")
public class PassengerController {

    private final PassengerService passengerService;

    @GetMapping("/list")
    @Operation(summary = "乘车人列表", description = "获取当前用户的乘车人列表")
    public Result<List<PassengerDTO>> listPassengers(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<PassengerDTO> passengers = passengerService.listPassengers(userId);
        return Result.ok(passengers);
    }

    @PostMapping("/add")
    @Operation(summary = "添加乘车人", description = "姓名+身份证+类型")
    public Result<PassengerDTO> addPassenger(Authentication authentication,
                                             @Valid @RequestBody AddPassengerRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PassengerDTO passenger = passengerService.addPassenger(userId, request);
        return Result.ok(passenger);
    }

    @PutMapping("/{id}")
    @Operation(summary = "修改乘车人", description = "修改乘车人信息")
    public Result<PassengerDTO> updatePassenger(Authentication authentication,
                                                @PathVariable Long id,
                                                @Valid @RequestBody UpdatePassengerRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PassengerDTO passenger = passengerService.updatePassenger(userId, id, request);
        return Result.ok(passenger);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除乘车人", description = "逻辑删除乘车人")
    public Result<Void> deletePassenger(Authentication authentication,
                                        @PathVariable Long id) {
        Long userId = (Long) authentication.getPrincipal();
        passengerService.deletePassenger(userId, id);
        return Result.ok();
    }
}
