package com.railmind.ticket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.railmind.common.exception.BizException;
import com.railmind.ticket.domain.model.TicketInventory;
import com.railmind.ticket.domain.service.InventoryDomainService;
import com.railmind.ticket.dto.TicketQueryRequest;
import com.railmind.ticket.service.impl.TicketQueryServiceImpl;
import com.railmind.ticket.vo.TicketQueryVO;
import com.railmind.train.domain.model.SeatType;
import com.railmind.train.domain.model.Station;
import com.railmind.train.domain.model.TicketPrice;
import com.railmind.train.domain.model.Train;
import com.railmind.train.mapper.SeatTypeMapper;
import com.railmind.train.mapper.StationMapper;
import com.railmind.train.mapper.TicketPriceMapper;
import com.railmind.train.mapper.TrainMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketQueryServiceTest {

    @Mock
    private TrainMapper trainMapper;

    @Mock
    private StationMapper stationMapper;

    @Mock
    private TicketPriceMapper ticketPriceMapper;

    @Mock
    private SeatTypeMapper seatTypeMapper;

    @Mock
    private InventoryDomainService inventoryDomainService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private Cache<String, TicketQueryVO> localCache;
    private Executor asyncExecutor;
    private ObjectMapper objectMapper;

    @InjectMocks
    private TicketQueryServiceImpl ticketQueryService;

    private Train testTrain;
    private Station fromStation;
    private Station toStation;

    @BeforeEach
    void setUp() {
        localCache = Caffeine.newBuilder().maximumSize(100).build();
        asyncExecutor = Executors.newFixedThreadPool(2);
        objectMapper = new ObjectMapper();

        ticketQueryService = new TicketQueryServiceImpl(
                trainMapper, stationMapper, ticketPriceMapper, seatTypeMapper,
                inventoryDomainService, redisTemplate, localCache, asyncExecutor, objectMapper);

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
                .id(1L).code("BJN").name("北京南站").city("北京").province("北京").build();

        toStation = Station.builder()
                .id(2L).code("AOH").name("上海虹桥站").city("上海").province("上海").build();
    }

    @Test
    void queryTicket_shouldReturnFromMySQL() {
        TicketQueryRequest request = new TicketQueryRequest();
        request.setTrainNo("G1");
        request.setTravelDate(LocalDate.now().plusDays(1));
        request.setFromStation("BJN");
        request.setToStation("AOH");

        when(trainMapper.selectByTrainNo("G1")).thenReturn(testTrain);
        when(stationMapper.selectByCode("BJN")).thenReturn(fromStation);
        when(stationMapper.selectByCode("AOH")).thenReturn(toStation);
        when(inventoryDomainService.getInventories(eq(1L), any(), eq("BJN"), eq("AOH")))
                .thenReturn(Arrays.asList(
                        TicketInventory.builder()
                                .id(1L).trainId(1L).travelDate(request.getTravelDate())
                                .fromStation("BJN").toStation("AOH").seatTypeCode("ZE")
                                .totalCount(868).soldCount(100).lockedCount(10).version(0L).build(),
                        TicketInventory.builder()
                                .id(2L).trainId(1L).travelDate(request.getTravelDate())
                                .fromStation("BJN").toStation("AOH").seatTypeCode("ZY")
                                .totalCount(168).soldCount(20).lockedCount(5).version(0L).build()
                ));
        when(ticketPriceMapper.selectByTrainAndRoute(1L, "BJN", "AOH"))
                .thenReturn(Arrays.asList(
                        TicketPrice.builder().trainId(1L).fromStation("BJN").toStation("AOH").seatTypeCode("ZE").price(new BigDecimal("553.00")).build(),
                        TicketPrice.builder().trainId(1L).fromStation("BJN").toStation("AOH").seatTypeCode("ZY").price(new BigDecimal("933.00")).build()
                ));
        when(seatTypeMapper.selectByTrainId(1L))
                .thenReturn(Arrays.asList(
                        SeatType.builder().id(1L).trainId(1L).seatTypeCode("ZE").seatTypeName("二等座").totalCount(868).priceFactor(new BigDecimal("1.00")).build(),
                        SeatType.builder().id(2L).trainId(1L).seatTypeCode("ZY").seatTypeName("一等座").totalCount(168).priceFactor(new BigDecimal("1.80")).build()
                ));

        TicketQueryVO result = ticketQueryService.queryTicket(request);

        assertNotNull(result);
        assertEquals("G1", result.getTrainNo());
        assertEquals("北京南站", result.getFromStationName());
        assertEquals("上海虹桥站", result.getToStationName());
        assertEquals(2, result.getSeatTickets().size());
        assertEquals(758, result.getSeatTickets().get(0).getRemainCount());
    }

    @Test
    void queryTicket_shouldThrowWhenTrainNotFound() {
        TicketQueryRequest request = new TicketQueryRequest();
        request.setTrainNo("INVALID");
        request.setTravelDate(LocalDate.now().plusDays(1));
        request.setFromStation("BJN");
        request.setToStation("AOH");

        when(trainMapper.selectByTrainNo("INVALID")).thenReturn(null);

        assertThrows(BizException.class, () -> ticketQueryService.queryTicket(request));
    }

    @Test
    void queryTicket_shouldThrowWhenStationNotFound() {
        TicketQueryRequest request = new TicketQueryRequest();
        request.setTrainNo("G1");
        request.setTravelDate(LocalDate.now().plusDays(1));
        request.setFromStation("INVALID");
        request.setToStation("AOH");

        when(trainMapper.selectByTrainNo("G1")).thenReturn(testTrain);
        when(stationMapper.selectByCode("INVALID")).thenReturn(null);

        assertThrows(BizException.class, () -> ticketQueryService.queryTicket(request));
    }

    @Test
    void queryTicket_shouldHitLocalCache() {
        TicketQueryRequest request = new TicketQueryRequest();
        request.setTrainNo("G1");
        request.setTravelDate(LocalDate.now().plusDays(1));
        request.setFromStation("BJN");
        request.setToStation("AOH");

        TicketQueryVO cached = TicketQueryVO.builder()
                .trainNo("G1").fromStationName("北京南站").toStationName("上海虹桥站")
                .seatTickets(List.of()).build();
        localCache.put("ticket:query:G1:" + request.getTravelDate() + ":BJN:AOH", cached);

        TicketQueryVO result = ticketQueryService.queryTicket(request);

        assertNotNull(result);
        assertEquals("G1", result.getTrainNo());
        verify(trainMapper, never()).selectByTrainNo(any());
    }
}
