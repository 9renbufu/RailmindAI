package com.railmind.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 50, message = "姓名最长50")
    private String realName;

    @Size(max = 100, message = "邮箱最长100")
    private String email;

    @Size(max = 500, message = "头像URL最长500")
    private String avatar;
}
