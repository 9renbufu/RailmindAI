package com.railmind.train.service;

import com.railmind.common.exception.BizException;
import com.railmind.train.domain.model.SeatType;
import com.railmind.train.domain.model.Station;
import com.railmind.train.domain.model.Train;
import com.railmind.train.domain.model.TrainStation;
import com.railmind.train.mapper.SeatTypeMapper;
import com.railmind.train.mapper.StationMapper;
import com.railmind.train.mapper.TrainMapper;
import com.railmind.train.mapper.TrainStationMapper;
import com.railmind.train.service.impl.TrainServiceImpl;
import com.railmind.train.vo.TrainVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainServiceTest {

    @Mock
    private TrainMapper trainMapper;

    @Mock
    private TrainStationMapper trainStationMapper;

    @Mock
    private SeatTypeMapper seatTypeMapper;

    @Mock
    private StationMapper stationMapper;

    @InjectMocks
    private TrainServiceImpl trainService;

    private Train testTrain;
    private Station startStation;
    private Station endStation;

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

        startStation = Station.builder()
                .id(1L)
                .code("BJN")
                .name("北京南站")
                .city("北京")
                .province("北京")
                .build();

        endStation = Station.builder()
                .id(2L)
                .code("AOH")
                .name("上海虹桥站")
                .city("上海")
                .province("上海")
                .build();
    }

    @Test
    void getTrainDetail_shouldReturnTrainWithDetails() {
        when(trainMapper.selectById(1L)).thenReturn(testTrain);
        when(stationMapper.selectByCode("BJN")).thenReturn(startStation);
        when(stationMapper.selectByCode("AOH")).thenReturn(endStation);
        when(trainStationMapper.selectByTrainId(1L)).thenReturn(Arrays.asList(
                TrainStation.builder()
                        .id(1L)
                        .trainId(1L)
                        .stationCode("BJN")
                        .stationOrder(1)
                        .departureTime(LocalTime.of(9, 0))
                        .mileage(0)
                        .build(),
                TrainStation.builder()
                        .id(2L)
                        .trainId(1L)
                        .stationCode("AOH")
                        .stationOrder(2)
                        .arrivalTime(LocalTime.of(13, 28))
                        .mileage(1318)
                        .build()
        ));
        when(seatTypeMapper.selectByTrainId(1L)).thenReturn(Arrays.asList(
                SeatType.builder()
                        .id(1L)
                        .trainId(1L)
                        .seatTypeCode("ZE")
                        .seatTypeName("二等座")
                        .totalCount(100)
                        .priceFactor(new BigDecimal("1.00"))
                        .build()
        ));

        TrainVO result = trainService.getTrainDetail(1L);

        assertNotNull(result);
        assertEquals("G1", result.getTrainNo());
        assertEquals("高铁", result.getTrainTypeName());
        assertEquals("北京南站", result.getStartStationName());
        assertEquals("上海虹桥站", result.getEndStationName());
        assertNotNull(result.getStations());
        assertEquals(2, result.getStations().size());
        assertNotNull(result.getSeatTypes());
        assertEquals(1, result.getSeatTypes().size());
    }

    @Test
    void getTrainDetail_shouldThrowWhenNotFound() {
        when(trainMapper.selectById(99L)).thenReturn(null);

        assertThrows(BizException.class, () -> trainService.getTrainDetail(99L));
    }

    @Test
    void getTrainByNo_shouldReturnTrain() {
        when(trainMapper.selectByTrainNo("G1")).thenReturn(testTrain);
        when(trainMapper.selectById(1L)).thenReturn(testTrain);
        when(stationMapper.selectByCode("BJN")).thenReturn(startStation);
        when(stationMapper.selectByCode("AOH")).thenReturn(endStation);
        when(trainStationMapper.selectByTrainId(1L)).thenReturn(Arrays.asList());
        when(seatTypeMapper.selectByTrainId(1L)).thenReturn(Arrays.asList());

        TrainVO result = trainService.getTrainByNo("G1");

        assertNotNull(result);
        assertEquals("G1", result.getTrainNo());
    }

    @Test
    void getTrainByNo_shouldThrowWhenNotFound() {
        when(trainMapper.selectByTrainNo("INVALID")).thenReturn(null);

        assertThrows(BizException.class, () -> trainService.getTrainByNo("INVALID"));
    }
}
