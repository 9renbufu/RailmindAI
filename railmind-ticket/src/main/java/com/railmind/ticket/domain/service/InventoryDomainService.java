package com.railmind.ticket.domain.service;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.ticket.domain.model.TicketInventory;
import com.railmind.ticket.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryDomainService {

    private final InventoryMapper inventoryMapper;

    /**
     * 扣减库存（乐观锁）
     */
    @Transactional
    public boolean deductStock(Long inventoryId, int count, Long version) {
        int rows = inventoryMapper.deductStock(inventoryId, count, version);
        if (rows == 0) {
            log.warn("库存扣减失败: inventoryId={}, count={}, version={}", inventoryId, count, version);
            return false;
        }
        log.info("库存扣减成功: inventoryId={}, count={}", inventoryId, count);
        return true;
    }

    /**
     * 回滚库存
     */
    @Transactional
    public void rollbackStock(Long inventoryId, int count) {
        inventoryMapper.rollbackStock(inventoryId, count);
        log.info("库存回滚成功: inventoryId={}, count={}", inventoryId, count);
    }

    /**
     * 锁定库存（乐观锁）
     */
    @Transactional
    public boolean lockStock(Long inventoryId, int count, Long version) {
        int rows = inventoryMapper.lockStock(inventoryId, count, version);
        if (rows == 0) {
            log.warn("库存锁定失败: inventoryId={}, count={}, version={}", inventoryId, count, version);
            return false;
        }
        log.info("库存锁定成功: inventoryId={}, count={}", inventoryId, count);
        return true;
    }

    /**
     * 解锁库存
     */
    @Transactional
    public void unlockStock(Long inventoryId, int count) {
        inventoryMapper.unlockStock(inventoryId, count);
        log.info("库存解锁成功: inventoryId={}, count={}", inventoryId, count);
    }

    /**
     * 查询指定车次、日期、区间的库存
     */
    public List<TicketInventory> getInventories(Long trainId, LocalDate travelDate, String fromStation, String toStation) {
        return inventoryMapper.selectByTrainAndRoute(trainId, travelDate, fromStation, toStation);
    }

    /**
     * 查询指定车次、日期的所有库存
     */
    public List<TicketInventory> getAllInventories(Long trainId, LocalDate travelDate) {
        return inventoryMapper.selectByTrainAndDate(trainId, travelDate);
    }

    /**
     * 查询单个库存记录
     */
    public TicketInventory getInventory(Long trainId, LocalDate travelDate, String fromStation, String toStation, String seatTypeCode) {
        return inventoryMapper.selectByUniqueKey(trainId, travelDate, fromStation, toStation, seatTypeCode);
    }

    /**
     * 计算剩余票数
     */
    public int calculateRemainCount(TicketInventory inventory) {
        return inventory.getTotalCount() - inventory.getSoldCount() - inventory.getLockedCount();
    }
}
