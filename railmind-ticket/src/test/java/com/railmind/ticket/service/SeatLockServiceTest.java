package com.railmind.ticket.service;

import com.railmind.common.exception.BizException;
import com.railmind.ticket.domain.model.SeatLock;
import com.railmind.ticket.dto.LockSeatRequest;
import com.railmind.ticket.mapper.SeatLockMapper;
import com.railmind.ticket.service.impl.SeatLockServiceImpl;
import com.railmind.ticket.vo.SeatLockVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("座位锁定服务测试")
class SeatLockServiceTest {

    @Mock
    private SeatLockMapper seatLockMapper;

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private RLock rLock;

    @InjectMocks
    private SeatLockServiceImpl seatLockService;

    private SeatLock testSeatLock;
    private LockSeatRequest testRequest;

    @BeforeEach
    void setUp() {
        testSeatLock = SeatLock.builder()
                .id(1L)
                .trainId(1L)
                .travelDate(LocalDate.of(2026, 7, 1))
                .seatTypeCode("SW")
                .seatNo("05车12A")
                .orderNo("ORDER001")
                .userId(1L)
                .lockTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusMinutes(15))
                .status(1)
                .build();

        testRequest = new LockSeatRequest();
        testRequest.setTrainId(1L);
        testRequest.setTravelDate(LocalDate.of(2026, 7, 1));
        testRequest.setFromStation("BJP");
        testRequest.setToStation("SHH");
        testRequest.setSeatTypeCode("SW");
        testRequest.setSeatNo("05车12A");
        testRequest.setUserId(1L);
        testRequest.setOrderNo("ORDER001");
        testRequest.setLockMinutes(15);
    }

    @Test
    @DisplayName("锁定座位成功")
    void lockSeat_Success() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(seatLockMapper.selectActiveLock(anyLong(), any(LocalDate.class), anyString(), anyString()))
                .thenReturn(null);
        when(seatLockMapper.insert(any(SeatLock.class))).thenReturn(1);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        SeatLockVO result = seatLockService.lockSeat(testRequest);

        assertNotNull(result);
        assertEquals(1L, result.getTrainId());
        assertEquals("05车12A", result.getSeatNo());
        assertEquals(1, result.getStatus());
        verify(seatLockMapper).insert(any(SeatLock.class));
    }

    @Test
    @DisplayName("锁定座位 - 同一用户同一订单返回现有锁")
    void lockSeat_SameUserSameOrder_ReturnExisting() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(seatLockMapper.selectActiveLock(anyLong(), any(LocalDate.class), anyString(), anyString()))
                .thenReturn(testSeatLock);

        SeatLockVO result = seatLockService.lockSeat(testRequest);

        assertNotNull(result);
        assertEquals(testSeatLock.getId(), result.getId());
    }

    @Test
    @DisplayName("锁定座位失败 - 座位已被其他用户锁定")
    void lockSeat_SeatAlreadyLockedByOther() throws InterruptedException {
        SeatLock otherUserLock = SeatLock.builder()
                .id(2L)
                .trainId(1L)
                .travelDate(LocalDate.of(2026, 7, 1))
                .seatTypeCode("SW")
                .seatNo("05车12A")
                .orderNo("ORDER002")
                .userId(2L)
                .lockTime(LocalDateTime.now())
                .expireTime(LocalDateTime.now().plusMinutes(15))
                .status(1)
                .build();

        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(seatLockMapper.selectActiveLock(anyLong(), any(LocalDate.class), anyString(), anyString()))
                .thenReturn(otherUserLock);

        assertThrows(BizException.class, () -> seatLockService.lockSeat(testRequest));
    }

    @Test
    @DisplayName("锁定座位失败 - 获取锁超时")
    void lockSeat_LockTimeout() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        assertThrows(BizException.class, () -> seatLockService.lockSeat(testRequest));
    }

    @Test
    @DisplayName("释放座位锁成功")
    void releaseLock_Success() {
        when(seatLockMapper.selectById(1L)).thenReturn(testSeatLock);
        when(seatLockMapper.releaseLock(1L)).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> seatLockService.releaseLock(1L, 1L));
        verify(seatLockMapper).releaseLock(1L);
    }

    @Test
    @DisplayName("释放座位锁失败 - 无权操作")
    void releaseLock_PermissionDenied() {
        when(seatLockMapper.selectById(1L)).thenReturn(testSeatLock);

        assertThrows(BizException.class, () -> seatLockService.releaseLock(1L, 2L));
    }

    @Test
    @DisplayName("根据订单号释放座位锁")
    void releaseLockByOrderNo_Success() {
        List<SeatLock> locks = Arrays.asList(testSeatLock);
        when(seatLockMapper.selectByOrderNo("ORDER001")).thenReturn(locks);
        when(seatLockMapper.releaseLock(1L)).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> seatLockService.releaseLockByOrderNo("ORDER001"));
        verify(seatLockMapper).releaseLock(1L);
    }

    @Test
    @DisplayName("获取已锁定座位列表")
    void getLockedSeats_Success() {
        List<String> lockedSeats = Arrays.asList("05车12A", "05车12B");
        when(seatLockMapper.selectLockedSeats(anyLong(), any(LocalDate.class), anyString()))
                .thenReturn(lockedSeats);

        List<String> result = seatLockService.getLockedSeats(1L, LocalDate.of(2026, 7, 1), "SW");

        assertEquals(2, result.size());
        assertTrue(result.contains("05车12A"));
    }

    @Test
    @DisplayName("获取用户活跃座位锁")
    void getUserActiveLocks_Success() {
        List<SeatLock> locks = Arrays.asList(testSeatLock);
        when(seatLockMapper.selectUserActiveLocks(1L)).thenReturn(locks);

        List<SeatLockVO> result = seatLockService.getUserActiveLocks(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    @DisplayName("清理过期座位锁")
    void cleanExpiredLocks_Success() {
        when(seatLockMapper.releaseExpiredLocks()).thenReturn(5);

        int count = seatLockService.cleanExpiredLocks();

        assertEquals(5, count);
    }
}
