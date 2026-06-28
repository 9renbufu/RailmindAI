package com.railmind.ticket.service;

import com.railmind.common.exception.BizException;
import com.railmind.ticket.domain.model.TicketInventory;
import com.railmind.ticket.domain.service.InventoryDomainService;
import com.railmind.ticket.dto.InventoryInitRequest;
import com.railmind.ticket.mapper.InventoryMapper;
import com.railmind.ticket.service.impl.InventoryServiceImpl;
import com.railmind.ticket.vo.InventoryDetailVO;
import com.railmind.train.domain.model.SeatType;
import com.railmind.train.domain.model.Station;
import com.railmind.train.domain.model.Train;
import com.railmind.train.domain.model.TrainStation;
import com.railmind.train.mapper.SeatTypeMapper;
import com.railmind.train.mapper.StationMapper;
import com.railmind.train.mapper.TrainMapper;
import com.railmind.train.mapper.TrainStationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryDomainService inventoryDomainService;

    @Mock
    private TrainMapper trainMapper;

    @Mock
    private StationMapper stationMapper;

    @Mock
    private TrainStationMapper trainStationMapper;

    @Mock
    private SeatTypeMapper seatTypeMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Train testTrain;
    private Station fromStation;
    private Station toStation;

    @BeforeEach
    void setUp() {
        testTrain = Train.builder()
                .id(1L)
                .trainNo("G1")
                .trainType("G")
                .startStation("BJN")
                .endStation("AOH")
                .departureTime(LocalTime.of(9, 0))
                .arrivalTime(LocalTime.of(13, 28))
                .runDays(1)
                .totalMileage(1318)
                .status(1)
                .build();

        fromStation = Station.builder()
                .id(1L).code("BJN").name("北京南站").build();

        toStation = Station.builder()
                .id(2L).code("AOH").name("上海虹桥站").build();
    }

    @Test
    void initInventory_shouldSucceed() {
        InventoryInitRequest request = new InventoryInitRequest();
        request.setTrainId(1L);
        request.setTravelDate(LocalDate.now().plusDays(1));
        request.setFromStation("BJN");
        request.setToStation("AOH");

        when(trainMapper.selectById(1L)).thenReturn(testTrain);
        when(trainStationMapper.selectByTrainId(1L)).thenReturn(Arrays.asList(
                TrainStation.builder().id(1L).trainId(1L).stationCode("BJN").stationOrder(1).mileage(0).build(),
                TrainStation.builder().id(2L).trainId(1L).stationCode("AOH").stationOrder(2).mileage(1318).build()
        ));
        when(seatTypeMapper.selectByTrainId(1L)).thenReturn(Arrays.asList(
                SeatType.builder().id(1L).trainId(1L).seatTypeCode("ZE").seatTypeName("二等座").totalCount(868).priceFactor(new BigDecimal("1.00")).build(),
                SeatType.builder().id(2L).trainId(1L).seatTypeCode("ZY").seatTypeName("一等座").totalCount(168).priceFactor(new BigDecimal("1.80")).build()
        ));
        when(inventoryMapper.selectByUniqueKey(eq(1L), any(), eq("BJN"), eq("AOH"), eq("ZE"))).thenReturn(null);
        when(inventoryMapper.selectByUniqueKey(eq(1L), any(), eq("BJN"), eq("AOH"), eq("ZY"))).thenReturn(null);
        when(inventoryMapper.insert(any(TicketInventory.class))).thenReturn(1);

        inventoryService.initInventory(request);

        verify(inventoryMapper, times(2)).insert(any(TicketInventory.class));
    }

    @Test
    void initInventory_shouldThrowWhenTrainNotFound() {
        InventoryInitRequest request = new InventoryInitRequest();
        request.setTrainId(99L);
        request.setTravelDate(LocalDate.now().plusDays(1));

        when(trainMapper.selectById(99L)).thenReturn(null);

        assertThrows(BizException.class, () -> inventoryService.initInventory(request));
    }

    @Test
    void initInventory_shouldSkipExistingInventory() {
        InventoryInitRequest request = new InventoryInitRequest();
        request.setTrainId(1L);
        request.setTravelDate(LocalDate.now().plusDays(1));
        request.setFromStation("BJN");
        request.setToStation("AOH");

        when(trainMapper.selectById(1L)).thenReturn(testTrain);
        when(trainStationMapper.selectByTrainId(1L)).thenReturn(Arrays.asList(
                TrainStation.builder().id(1L).trainId(1L).stationCode("BJN").stationOrder(1).mileage(0).build(),
                TrainStation.builder().id(2L).trainId(1L).stationCode("AOH").stationOrder(2).mileage(1318).build()
        ));
        when(seatTypeMapper.selectByTrainId(1L)).thenReturn(Arrays.asList(
                SeatType.builder().id(1L).trainId(1L).seatTypeCode("ZE").seatTypeName("二等座").totalCount(868).priceFactor(new BigDecimal("1.00")).build()
        ));
        when(inventoryMapper.selectByUniqueKey(eq(1L), any(), eq("BJN"), eq("AOH"), eq("ZE")))
                .thenReturn(TicketInventory.builder().id(1L).build());

        inventoryService.initInventory(request);

        verify(inventoryMapper, never()).insert(any(TicketInventory.class));
    }

    @Test
    void getInventoryDetail_shouldReturnDetail() {
        when(trainMapper.selectById(1L)).thenReturn(testTrain);
        when(inventoryDomainService.getAllInventories(1L, LocalDate.now().plusDays(1)))
                .thenReturn(Arrays.asList(
                        TicketInventory.builder()
                                .id(1L).trainId(1L).travelDate(LocalDate.now().plusDays(1))
                                .fromStation("BJN").toStation("AOH").seatTypeCode("ZE")
                                .totalCount(868).soldCount(100).lockedCount(10).version(0L).build()
                ));
        when(stationMapper.selectByCode("BJN")).thenReturn(fromStation);
        when(stationMapper.selectByCode("AOH")).thenReturn(toStation);
        when(seatTypeMapper.selectByTrainIdAndCode(1L, "ZE"))
                .thenReturn(SeatType.builder().seatTypeCode("ZE").seatTypeName("二等座").build());

        InventoryDetailVO result = inventoryService.getInventoryDetail(1L, LocalDate.now().plusDays(1).toString());

        assertNotNull(result);
        assertEquals("G1", result.getTrainNo());
        assertEquals(1, result.getRouteInventories().size());
        assertEquals(758, result.getRouteInventories().get(0).getRemainCount());
    }

    @Test
    void getInventoryDetail_shouldThrowWhenTrainNotFound() {
        when(trainMapper.selectById(99L)).thenReturn(null);

        assertThrows(BizException.class, () -> inventoryService.getInventoryDetail(99L, "2026-07-01"));
    }
}
