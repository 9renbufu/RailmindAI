package com.railmind.user.service;

import com.railmind.common.util.CryptoUtil;
import com.railmind.user.domain.model.User;
import com.railmind.user.domain.repository.UserRepository;
import com.railmind.user.domain.service.UserDomainService;
import com.railmind.user.dto.UpdateUserRequest;
import com.railmind.user.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;

    public UserDTO getUserProfile(Long userId) {
        User user = userDomainService.getActiveUser(userId);
        return toUserDTO(user);
    }

    @Transactional
    public UserDTO updateUserProfile(Long userId, UpdateUserRequest request) {
        log.info("Updating user profile: userId={}", userId);

        User user = userDomainService.getActiveUser(userId);

        if (request.getRealName() != null) {
            user.setRealName(request.getRealName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        user = userRepository.save(user);
        log.info("User profile updated: userId={}", userId);

        return toUserDTO(user);
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
