package com.railmind.ticket.service;

import com.railmind.ticket.dto.InventoryInitRequest;
import com.railmind.ticket.vo.InventoryDetailVO;

public interface InventoryService {

    /**
     * 初始化库存
     */
    void initInventory(InventoryInitRequest request);

    /**
     * 查询库存详情
     */
    InventoryDetailVO getInventoryDetail(Long trainId, String travelDate);
}
