package com.railmind.ticket.service;

import com.railmind.ticket.dto.WaitlistCancelRequest;
import com.railmind.ticket.dto.WaitlistJoinRequest;
import com.railmind.ticket.vo.WaitlistPositionVO;
import com.railmind.ticket.vo.WaitlistVO;

import java.util.List;

public interface WaitlistService {

    /**
     * 加入候补队列
     */
    WaitlistVO joinWaitlist(WaitlistJoinRequest request);

    /**
     * 取消候补
     */
    void cancelWaitlist(WaitlistCancelRequest request);

    /**
     * 获取用户候补列表
     */
    List<WaitlistVO> getUserWaitlist(Long userId);

    /**
     * 获取候补排队位置
     */
    WaitlistPositionVO getWaitlistPosition(Long waitlistId);

    /**
     * 处理候补兑现（有退票时调用）
     */
    List<WaitlistVO> fulfillWaitlist(Long trainId, java.time.LocalDate travelDate,
                                     String fromStation, String toStation,
                                     String seatTypeCode, int count);

    /**
     * 取消过期的候补（定时任务调用）
     */
    int cancelExpiredWaitlist();
}
