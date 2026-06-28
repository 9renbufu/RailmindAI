package com.railmind.ticket.service.impl;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.ticket.domain.model.TicketInventory;
import com.railmind.ticket.domain.service.InventoryDomainService;
import com.railmind.ticket.dto.InventoryInitRequest;
import com.railmind.ticket.mapper.InventoryMapper;
import com.railmind.ticket.service.InventoryService;
import com.railmind.ticket.vo.InventoryDetailVO;
import com.railmind.train.domain.model.SeatType;
import com.railmind.train.domain.model.Station;
import com.railmind.train.domain.model.Train;
import com.railmind.train.domain.model.TrainStation;
import com.railmind.train.mapper.SeatTypeMapper;
import com.railmind.train.mapper.StationMapper;
import com.railmind.train.mapper.TrainMapper;
import com.railmind.train.mapper.TrainStationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryDomainService inventoryDomainService;
    private final TrainMapper trainMapper;
    private final StationMapper stationMapper;
    private final TrainStationMapper trainStationMapper;
    private final SeatTypeMapper seatTypeMapper;

    @Override
    @Transactional
    public void initInventory(InventoryInitRequest request) {
        log.info("初始化库存: trainId={}, date={}", request.getTrainId(), request.getTravelDate());

        Train train = trainMapper.selectById(request.getTrainId());
        if (train == null) {
            throw new BizException(ErrorCode.TRAIN_NOT_FOUND);
        }

        List<TrainStation> stations = trainStationMapper.selectByTrainId(train.getId());
        if (stations.size() < 2) {
            throw new BizException(ErrorCode.SCHEDULE_NOT_FOUND);
        }

        List<SeatType> seatTypes = seatTypeMapper.selectByTrainId(train.getId());

        if (request.getFromStation() != null && request.getToStation() != null) {
            initForRoute(train, request.getTravelDate(), request.getFromStation(), request.getToStation(), seatTypes);
        } else {
            initForAllRoutes(train, request.getTravelDate(), stations, seatTypes);
        }

        log.info("库存初始化完成: trainId={}, date={}", request.getTrainId(), request.getTravelDate());
    }

    private void initForAllRoutes(Train train, LocalDate travelDate, List<TrainStation> stations, List<SeatType> seatTypes) {
        for (int i = 0; i < stations.size() - 1; i++) {
            for (int j = i + 1; j < stations.size(); j++) {
                String fromStation = stations.get(i).getStationCode();
                String toStation = stations.get(j).getStationCode();
                initForRoute(train, travelDate, fromStation, toStation, seatTypes);
            }
        }
    }

    private void initForRoute(Train train, LocalDate travelDate, String fromStation, String toStation, List<SeatType> seatTypes) {
        for (SeatType seatType : seatTypes) {
            if (seatType.getTotalCount() == 0) continue;

            TicketInventory existing = inventoryMapper.selectByUniqueKey(
                    train.getId(), travelDate, fromStation, toStation, seatType.getSeatTypeCode());

            if (existing == null) {
                TicketInventory inventory = TicketInventory.builder()
                        .trainId(train.getId())
                        .travelDate(travelDate)
                        .fromStation(fromStation)
                        .toStation(toStation)
                        .seatTypeCode(seatType.getSeatTypeCode())
                        .totalCount(seatType.getTotalCount())
                        .soldCount(0)
                        .lockedCount(0)
                        .version(0L)
                        .build();
                inventoryMapper.insert(inventory);
                log.debug("初始化库存: trainId={}, date={}, from={}, to={}, seatType={}, total={}",
                        train.getId(), travelDate, fromStation, toStation, seatType.getSeatTypeCode(), seatType.getTotalCount());
            }
        }
    }

    @Override
    public InventoryDetailVO getInventoryDetail(Long trainId, String travelDate) {
        log.info("查询库存详情: trainId={}, date={}", trainId, travelDate);

        Train train = trainMapper.selectById(trainId);
        if (train == null) {
            throw new BizException(ErrorCode.TRAIN_NOT_FOUND);
        }

        LocalDate date = LocalDate.parse(travelDate);
        List<TicketInventory> inventories = inventoryDomainService.getAllInventories(trainId, date);

        List<InventoryDetailVO.RouteInventoryVO> routeInventories = inventories.stream()
                .map(inv -> {
                    Station fromStation = stationMapper.selectByCode(inv.getFromStation());
                    Station toStation = stationMapper.selectByCode(inv.getToStation());
                    SeatType seatType = seatTypeMapper.selectByTrainIdAndCode(trainId, inv.getSeatTypeCode());

                    return InventoryDetailVO.RouteInventoryVO.builder()
                            .fromStation(inv.getFromStation())
                            .fromStationName(fromStation != null ? fromStation.getName() : null)
                            .toStation(inv.getToStation())
                            .toStationName(toStation != null ? toStation.getName() : null)
                            .seatTypeCode(inv.getSeatTypeCode())
                            .seatTypeName(seatType != null ? seatType.getSeatTypeName() : null)
                            .totalCount(inv.getTotalCount())
                            .soldCount(inv.getSoldCount())
                            .lockedCount(inv.getLockedCount())
                            .remainCount(inv.getTotalCount() - inv.getSoldCount() - inv.getLockedCount())
                            .version(inv.getVersion())
                            .build();
                })
                .collect(Collectors.toList());

        return InventoryDetailVO.builder()
                .trainId(trainId)
                .trainNo(train.getTrainNo())
                .travelDate(date)
                .routeInventories(routeInventories)
                .build();
    }
}
