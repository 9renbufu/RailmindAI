package com.railmind.user.service;

import com.railmind.common.exception.BizException;
import com.railmind.user.domain.model.User;
import com.railmind.user.domain.service.UserDomainService;
import com.railmind.user.dto.UpdateUserRequest;
import com.railmind.user.dto.UserDTO;
import com.railmind.user.mapper.UserMapper;
import com.railmind.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserDomainService userDomainService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("$2a$10$encodedPassword")
                .phone("13800001111")
                .realName("张三")
                .email("zhangsan@test.com")
                .status(1)
                .userLevel(1)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getUserProfile_shouldReturnUserDTO() {
        when(userDomainService.getActiveUser(1L)).thenReturn(testUser);

        UserDTO result = userService.getUserProfile(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("张三", result.getRealName());
        assertEquals("13800001111", result.getPhone());
        assertEquals("zhangsan@test.com", result.getEmail());
        assertEquals(1, result.getStatus());
        assertEquals(1, result.getUserLevel());
    }

    @Test
    void getUserProfile_shouldFailWhenUserNotFound() {
        when(userDomainService.getActiveUser(99L)).thenThrow(new BizException(1001, "用户不存在"));

        assertThrows(BizException.class, () -> userService.getUserProfile(99L));
    }

    @Test
    void updateUserProfile_shouldUpdateRealName() {
        when(userDomainService.getActiveUser(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRealName("李四");

        UserDTO result = userService.updateUserProfile(1L, request);

        assertNotNull(result);
        assertEquals("李四", result.getRealName());
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void updateUserProfile_shouldUpdateEmail() {
        when(userDomainService.getActiveUser(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setEmail("newemail@test.com");

        UserDTO result = userService.updateUserProfile(1L, request);

        assertNotNull(result);
        assertEquals("newemail@test.com", result.getEmail());
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void updateUserProfile_shouldUpdateAvatar() {
        when(userDomainService.getActiveUser(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setAvatar("https://example.com/avatar.jpg");

        UserDTO result = userService.updateUserProfile(1L, request);

        assertNotNull(result);
        assertEquals("https://example.com/avatar.jpg", result.getAvatar());
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void updateUserProfile_shouldUpdateMultipleFields() {
        when(userDomainService.getActiveUser(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRealName("王五");
        request.setEmail("wangwu@test.com");
        request.setAvatar("https://example.com/new-avatar.jpg");

        UserDTO result = userService.updateUserProfile(1L, request);

        assertNotNull(result);
        assertEquals("王五", result.getRealName());
        assertEquals("wangwu@test.com", result.getEmail());
        assertEquals("https://example.com/new-avatar.jpg", result.getAvatar());
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void updateUserProfile_shouldNotUpdateNullFields() {
        when(userDomainService.getActiveUser(1L)).thenReturn(testUser);
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        UpdateUserRequest request = new UpdateUserRequest();
        // All fields are null

        UserDTO result = userService.updateUserProfile(1L, request);

        assertNotNull(result);
        // Original values should be preserved
        assertEquals("张三", result.getRealName());
        assertEquals("zhangsan@test.com", result.getEmail());
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    void updateUserProfile_shouldFailWhenUserNotFound() {
        when(userDomainService.getActiveUser(99L)).thenThrow(new BizException(1001, "用户不存在"));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setRealName("新名字");

        assertThrows(BizException.class, () -> userService.updateUserProfile(99L, request));
    }
}
