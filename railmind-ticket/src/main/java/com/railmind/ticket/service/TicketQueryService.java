package com.railmind.ticket.service;

import com.railmind.ticket.dto.TicketQueryRequest;
import com.railmind.ticket.vo.TicketQueryVO;

import java.util.List;

public interface TicketQueryService {

    /**
     * 查询单个车次余票
     */
    TicketQueryVO queryTicket(TicketQueryRequest request);

    /**
     * 批量查询余票
     */
    List<TicketQueryVO> batchQueryTicket(List<TicketQueryRequest> requests);
}
