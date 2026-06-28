package com.railmind.ticket.service;

import com.railmind.common.exception.BizException;
import com.railmind.ticket.domain.model.Waitlist;
import com.railmind.ticket.dto.WaitlistCancelRequest;
import com.railmind.ticket.dto.WaitlistJoinRequest;
import com.railmind.ticket.mapper.WaitlistMapper;
import com.railmind.ticket.service.impl.WaitlistServiceImpl;
import com.railmind.ticket.vo.WaitlistPositionVO;
import com.railmind.ticket.vo.WaitlistVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("候补购票服务测试")
class WaitlistServiceTest {

    @Mock
    private WaitlistMapper waitlistMapper;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private WaitlistServiceImpl waitlistService;

    private Waitlist testWaitlist;
    private WaitlistJoinRequest testJoinRequest;
    private WaitlistCancelRequest testCancelRequest;

    @BeforeEach
    void setUp() {
        testWaitlist = Waitlist.builder()
                .id(1L)
                .userId(1L)
                .trainId(1L)
                .travelDate(LocalDate.of(2026, 7, 1))
                .fromStation("BJP")
                .toStation("SHH")
                .seatTypeCode("SW")
                .passengerIds("[1,2]")
                .priority(100)
                .status("WAITING")
                .expireAt(LocalDateTime.now().plusHours(24))
                .createdAt(LocalDateTime.now())
                .build();

        testJoinRequest = new WaitlistJoinRequest();
        testJoinRequest.setUserId(1L);
        testJoinRequest.setTrainId(1L);
        testJoinRequest.setTravelDate(LocalDate.of(2026, 7, 1));
        testJoinRequest.setFromStation("BJP");
        testJoinRequest.setToStation("SHH");
        testJoinRequest.setSeatTypeCode("SW");
        testJoinRequest.setPassengerIds("[1,2]");

        testCancelRequest = new WaitlistCancelRequest();
        testCancelRequest.setWaitlistId(1L);
        testCancelRequest.setUserId(1L);
    }

    @Test
    @DisplayName("加入候补队列成功")
    void joinWaitlist_Success() {
        when(waitlistMapper.selectByRoute(anyLong(), any(LocalDate.class), anyString(), anyString(), anyString()))
                .thenReturn(Arrays.asList());
        when(waitlistMapper.insert(any(Waitlist.class))).thenReturn(1);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        WaitlistVO result = waitlistService.joinWaitlist(testJoinRequest);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals("WAITING", result.getStatus());
        verify(waitlistMapper).insert(any(Waitlist.class));
    }

    @Test
    @DisplayName("加入候补队列失败 - 已在队列中")
    void joinWaitlist_AlreadyInQueue() {
        when(waitlistMapper.selectByRoute(anyLong(), any(LocalDate.class), anyString(), anyString(), anyString()))
                .thenReturn(Arrays.asList(testWaitlist));

        assertThrows(BizException.class, () -> waitlistService.joinWaitlist(testJoinRequest));
    }

    @Test
    @DisplayName("取消候补成功")
    void cancelWaitlist_Success() {
        when(waitlistMapper.selectById(1L)).thenReturn(testWaitlist);
        when(waitlistMapper.updateStatus(1L, "CANCELLED")).thenReturn(1);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        assertDoesNotThrow(() -> waitlistService.cancelWaitlist(testCancelRequest));
        verify(waitlistMapper).updateStatus(1L, "CANCELLED");
    }

    @Test
    @DisplayName("取消候补失败 - 无权操作")
    void cancelWaitlist_PermissionDenied() {
        testCancelRequest.setUserId(2L);
        when(waitlistMapper.selectById(1L)).thenReturn(testWaitlist);

        assertThrows(BizException.class, () -> waitlistService.cancelWaitlist(testCancelRequest));
    }

    @Test
    @DisplayName("取消候补失败 - 已处理")
    void cancelWaitlist_AlreadyProcessed() {
        testWaitlist.setStatus("FULFILLED");
        when(waitlistMapper.selectById(1L)).thenReturn(testWaitlist);

        assertThrows(BizException.class, () -> waitlistService.cancelWaitlist(testCancelRequest));
    }

    @Test
    @DisplayName("获取用户候补列表")
    void getUserWaitlist_Success() {
        List<Waitlist> waitlistList = Arrays.asList(testWaitlist);
        when(waitlistMapper.selectUserWaitlist(1L)).thenReturn(waitlistList);

        List<WaitlistVO> result = waitlistService.getUserWaitlist(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    @DisplayName("获取候补排队位置 - 候补中")
    void getWaitlistPosition_Waiting() {
        when(waitlistMapper.selectById(1L)).thenReturn(testWaitlist);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        Set<String> members = new HashSet<>();
        members.add("2");
        members.add("1");
        when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(members);

        WaitlistPositionVO result = waitlistService.getWaitlistPosition(1L);

        assertNotNull(result);
        assertEquals(1L, result.getWaitlistId());
        assertEquals("WAITING", result.getStatus());
    }

    @Test
    @DisplayName("获取候补排队位置 - 已兑现")
    void getWaitlistPosition_Fulfilled() {
        testWaitlist.setStatus("FULFILLED");
        when(waitlistMapper.selectById(1L)).thenReturn(testWaitlist);

        WaitlistPositionVO result = waitlistService.getWaitlistPosition(1L);

        assertNotNull(result);
        assertEquals("FULFILLED", result.getStatus());
    }

    @Test
    @DisplayName("处理候补兑现成功")
    void fulfillWaitlist_Success() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        Set<ZSetOperations.TypedTuple<String>> entries = new HashSet<>();
        ZSetOperations.TypedTuple<String> tuple = ZSetOperations.TypedTuple.of("1", 100.0);
        entries.add(tuple);
        when(zSetOperations.reverseRangeWithScores(anyString(), anyLong(), anyLong())).thenReturn(entries);

        when(waitlistMapper.selectById(1L)).thenReturn(testWaitlist);
        when(waitlistMapper.updateStatus(1L, "FULFILLED")).thenReturn(1);

        List<WaitlistVO> result = waitlistService.fulfillWaitlist(
                1L, LocalDate.of(2026, 7, 1), "BJP", "SHH", "SW", 1);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    @DisplayName("取消过期候补")
    void cancelExpiredWaitlist_Success() {
        when(waitlistMapper.cancelExpiredWaitlist()).thenReturn(3);

        int count = waitlistService.cancelExpiredWaitlist();

        assertEquals(3, count);
    }
}
