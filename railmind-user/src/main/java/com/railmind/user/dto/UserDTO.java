package com.railmind.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String maskedIdCard;
    private String email;
    private String avatar;
    private Integer status;
    private Integer userLevel;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createdAt;
}
