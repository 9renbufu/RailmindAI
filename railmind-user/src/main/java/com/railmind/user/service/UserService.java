package com.railmind.user.service;

import com.railmind.user.dto.UpdateUserRequest;
import com.railmind.user.dto.UserDTO;

public interface UserService {

    UserDTO getUserProfile(Long userId);

    UserDTO updateUserProfile(Long userId, UpdateUserRequest request);
}
