package com.railmind.user.domain.service;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.user.domain.model.User;
import com.railmind.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;

    public void checkPhoneAvailable(String phone) {
        if (userRepository.existsByPhone(phone)) {
            throw new BizException(ErrorCode.USER_PHONE_EXISTS);
        }
    }

    public void checkUsernameAvailable(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new BizException(ErrorCode.USER_USERNAME_EXISTS);
        }
    }

    public User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND));
        if (user.getDeleted() == 1) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BizException(ErrorCode.USER_DISABLED);
        }
        return user;
    }

    public User getActiveUserByPhone(String phone) {
        return userRepository.findByPhoneAndDeleted(phone, 0)
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND));
    }

    public User getActiveUserByUsername(String username) {
        return userRepository.findByUsernameAndDeleted(username, 0)
                .orElseThrow(() -> new BizException(ErrorCode.USER_NOT_FOUND));
    }

    public void updateLoginInfo(User user, String ip) {
        user.setLastLoginTime(java.time.LocalDateTime.now());
        user.setLastLoginIp(ip);
        userRepository.save(user);
    }
}
