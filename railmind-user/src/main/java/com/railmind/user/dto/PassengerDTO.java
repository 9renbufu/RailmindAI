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
public class PassengerDTO {

    private Long id;
    private Long userId;
    private String name;
    private String maskedIdCard;
    private String phone;
    private Integer type;
    private String typeName;
    private Integer status;
    private LocalDateTime createdAt;
}
