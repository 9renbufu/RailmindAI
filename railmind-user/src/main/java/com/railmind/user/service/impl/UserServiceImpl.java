package com.railmind.user.service.impl;

import com.railmind.common.util.CryptoUtil;
import com.railmind.user.domain.model.User;
import com.railmind.user.domain.service.UserDomainService;
import com.railmind.user.dto.UpdateUserRequest;
import com.railmind.user.dto.UserDTO;
import com.railmind.user.mapper.UserMapper;
import com.railmind.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserDomainService userDomainService;

    @Override
    public UserDTO getUserProfile(Long userId) {
        User user = userDomainService.getActiveUser(userId);
        return toUserDTO(user);
    }

    @Override
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

        userMapper.updateById(user);
        log.info("User profile updated: userId={}", userId);

        return toUserDTO(user);
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
