package com.railmind.ticket.service;

import com.railmind.ticket.dto.LockSeatRequest;
import com.railmind.ticket.vo.SeatLockVO;

import java.util.List;

public interface SeatLockService {

    /**
     * 锁定座位（Redisson分布式锁 + 数据库记录）
     */
    SeatLockVO lockSeat(LockSeatRequest request);

    /**
     * 释放座位锁
     */
    void releaseLock(Long lockId, Long userId);

    /**
     * 根据订单号释放所有座位锁
     */
    void releaseLockByOrderNo(String orderNo);

    /**
     * 获取指定车次/日期/座位类型的已锁定座位列表
     */
    List<String> getLockedSeats(Long trainId, java.time.LocalDate travelDate, String seatTypeCode);

    /**
     * 获取用户当前活跃的座位锁
     */
    List<SeatLockVO> getUserActiveLocks(Long userId);

    /**
     * 获取座位锁详情
     */
    SeatLockVO getLockDetail(Long lockId);

    /**
     * 清理过期的座位锁（定时任务调用）
     */
    int cleanExpiredLocks();
}
