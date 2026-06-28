package com.railmind.user.domain.service;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.user.domain.model.User;
import com.railmind.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDomainService {

    private final UserMapper userMapper;

    public void checkPhoneAvailable(String phone) {
        if (userMapper.countByPhone(phone) > 0) {
            throw new BizException(ErrorCode.USER_PHONE_EXISTS);
        }
    }

    public void checkUsernameAvailable(String username) {
        if (userMapper.countByUsername(username) > 0) {
            throw new BizException(ErrorCode.USER_USERNAME_EXISTS);
        }
    }

    public User getActiveUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        if (user.getStatus() == 0) {
            throw new BizException(ErrorCode.USER_DISABLED);
        }
        return user;
    }

    public User getActiveUserByPhone(String phone) {
        User user = userMapper.selectByPhoneAndDeleted(phone, 0);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public User getActiveUserByUsername(String username) {
        User user = userMapper.selectByUsernameAndDeleted(username, 0);
        if (user == null) {
            throw new BizException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public void updateLoginInfo(User user, String ip) {
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(ip);
        userMapper.updateById(user);
    }
}
