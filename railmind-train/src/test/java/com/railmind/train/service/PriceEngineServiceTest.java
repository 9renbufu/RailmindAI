package com.railmind.train.service;

import com.railmind.common.exception.BizException;
import com.railmind.train.domain.model.Station;
import com.railmind.train.domain.model.TicketPrice;
import com.railmind.train.domain.model.Train;
import com.railmind.train.domain.model.TrainStation;
import com.railmind.train.mapper.StationMapper;
import com.railmind.train.mapper.TicketPriceMapper;
import com.railmind.train.mapper.TrainMapper;
import com.railmind.train.mapper.TrainStationMapper;
import com.railmind.train.service.impl.PriceEngineServiceImpl;
import com.railmind.train.vo.PriceVO;
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
class PriceEngineServiceTest {

    @Mock
    private TrainMapper trainMapper;

    @Mock
    private StationMapper stationMapper;

    @Mock
    private TrainStationMapper trainStationMapper;

    @Mock
    private TicketPriceMapper ticketPriceMapper;

    @InjectMocks
    private PriceEngineServiceImpl priceEngineService;

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
                .totalMileage(1318)
                .status(1)
                .build();

        fromStation = Station.builder()
                .id(1L)
                .code("BJN")
                .name("北京南站")
                .build();

        toStation = Station.builder()
                .id(2L)
                .code("AOH")
                .name("上海虹桥站")
                .build();
    }

    @Test
    void queryPrice_shouldReturnPriceInfo() {
        when(trainMapper.selectByTrainNo("G1")).thenReturn(testTrain);
        when(stationMapper.selectByCode("BJN")).thenReturn(fromStation);
        when(stationMapper.selectByCode("AOH")).thenReturn(toStation);
        when(trainStationMapper.selectByTrainIdAndStationCode(1L, "BJN")).thenReturn(
                TrainStation.builder().trainId(1L).stationCode("BJN").mileage(0).build());
        when(trainStationMapper.selectByTrainIdAndStationCode(1L, "AOH")).thenReturn(
                TrainStation.builder().trainId(1L).stationCode("AOH").mileage(1318).build());
        when(ticketPriceMapper.selectByTrainAndRoute(1L, "BJN", "AOH")).thenReturn(Arrays.asList(
                TicketPrice.builder()
                        .trainId(1L)
                        .fromStation("BJN")
                        .toStation("AOH")
                        .seatTypeCode("ZE")
                        .price(new BigDecimal("553.00"))
                        .build(),
                TicketPrice.builder()
                        .trainId(1L)
                        .fromStation("BJN")
                        .toStation("AOH")
                        .seatTypeCode("ZY")
                        .price(new BigDecimal("933.00"))
                        .build()
        ));

        PriceVO result = priceEngineService.queryPrice("G1", "BJN", "AOH");

        assertNotNull(result);
        assertEquals("G1", result.getTrainNo());
        assertEquals("北京南站", result.getFromStationName());
        assertEquals("上海虹桥站", result.getToStationName());
        assertEquals(1318, result.getMileage());
        assertNotNull(result.getSeatPrices());
        assertEquals(2, result.getSeatPrices().size());
    }

    @Test
    void queryPrice_shouldThrowWhenTrainNotFound() {
        when(trainMapper.selectByTrainNo("INVALID")).thenReturn(null);

        assertThrows(BizException.class, () -> priceEngineService.queryPrice("INVALID", "BJP", "SHH"));
    }

    @Test
    void queryPrice_shouldThrowWhenStationNotFound() {
        when(trainMapper.selectByTrainNo("G1")).thenReturn(testTrain);
        when(stationMapper.selectByCode("INVALID")).thenReturn(null);

        assertThrows(BizException.class, () -> priceEngineService.queryPrice("G1", "INVALID", "AOH"));
    }
}
