package com.railmind.ticket.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.ticket.domain.model.TicketInventory;
import com.railmind.ticket.domain.service.InventoryDomainService;
import com.railmind.ticket.dto.TicketQueryRequest;
import com.railmind.ticket.service.TicketQueryService;
import com.railmind.ticket.vo.SeatTypeTicketVO;
import com.railmind.ticket.vo.TicketQueryVO;
import com.railmind.train.domain.model.SeatType;
import com.railmind.train.domain.model.Station;
import com.railmind.train.domain.model.TicketPrice;
import com.railmind.train.domain.model.Train;
import com.railmind.train.mapper.SeatTypeMapper;
import com.railmind.train.mapper.StationMapper;
import com.railmind.train.mapper.TicketPriceMapper;
import com.railmind.train.mapper.TrainMapper;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TicketQueryServiceImpl implements TicketQueryService {

    private final TrainMapper trainMapper;
    private final StationMapper stationMapper;
    private final TicketPriceMapper ticketPriceMapper;
    private final SeatTypeMapper seatTypeMapper;
    private final InventoryDomainService inventoryDomainService;
    private final StringRedisTemplate redisTemplate;
    private final Cache<String, TicketQueryVO> localCache;
    private final Executor asyncExecutor;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY_PREFIX = "ticket:query:";
    private static final long REDIS_CACHE_TTL_SECONDS = 30;

    private static final Map<String, String> TRAIN_TYPE_NAMES = Map.of(
            "G", "高铁", "D", "动车", "C", "城际",
            "Z", "直达", "T", "特快", "K", "快速", "L", "临客"
    );

    public TicketQueryServiceImpl(TrainMapper trainMapper,
                                   StationMapper stationMapper,
                                   TicketPriceMapper ticketPriceMapper,
                                   SeatTypeMapper seatTypeMapper,
                                   InventoryDomainService inventoryDomainService,
                                   StringRedisTemplate redisTemplate,
                                   Cache<String, TicketQueryVO> localCache,
                                   Executor asyncExecutor,
                                   ObjectMapper objectMapper) {
        this.trainMapper = trainMapper;
        this.stationMapper = stationMapper;
        this.ticketPriceMapper = ticketPriceMapper;
        this.seatTypeMapper = seatTypeMapper;
        this.inventoryDomainService = inventoryDomainService;
        this.redisTemplate = redisTemplate;
        this.localCache = localCache;
        this.asyncExecutor = asyncExecutor;
        this.objectMapper = objectMapper;
    }

    @Override
    public TicketQueryVO queryTicket(TicketQueryRequest request) {
        String cacheKey = buildCacheKey(request);
        log.info("余票查询: trainNo={}, date={}, from={}, to={}",
                request.getTrainNo(), request.getTravelDate(), request.getFromStation(), request.getToStation());

        // L1: Caffeine 本地缓存
        TicketQueryVO cached = localCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("命中本地缓存: {}", cacheKey);
            return cached;
        }

        // L2: Redis 缓存
        try {
            String redisValue = redisTemplate.opsForValue().get(cacheKey);
            if (redisValue != null) {
                log.debug("命中Redis缓存: {}", cacheKey);
                TicketQueryVO result = deserialize(redisValue);
                localCache.put(cacheKey, result);
                return result;
            }
        } catch (Exception e) {
            log.warn("Redis查询异常，降级到MySQL: {}", e.getMessage());
        }

        // L3: MySQL 回源查询
        TicketQueryVO result = queryFromMySQL(request);
        if (result == null) {
            throw new BizException(ErrorCode.TRAIN_NOT_FOUND);
        }

        // 写入缓存
        try {
            localCache.put(cacheKey, result);
            redisTemplate.opsForValue().set(cacheKey, serialize(result), Duration.ofSeconds(REDIS_CACHE_TTL_SECONDS));
        } catch (Exception e) {
            log.warn("缓存写入异常: {}", e.getMessage());
        }

        return result;
    }

    @Override
    public List<TicketQueryVO> batchQueryTicket(List<TicketQueryRequest> requests) {
        log.info("批量余票查询: {}个车次", requests.size());

        List<CompletableFuture<TicketQueryVO>> futures = requests.stream()
                .map(request -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return queryTicket(request);
                    } catch (Exception e) {
                        log.warn("批量查询中单个车次异常: trainNo={}, error={}", request.getTrainNo(), e.getMessage());
                        return null;
                    }
                }, asyncExecutor))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(result -> result != null)
                .collect(Collectors.toList());
    }

    private TicketQueryVO queryFromMySQL(TicketQueryRequest request) {
        Train train = trainMapper.selectByTrainNo(request.getTrainNo());
        if (train == null) {
            return null;
        }

        Station fromStation = stationMapper.selectByCode(request.getFromStation());
        Station toStation = stationMapper.selectByCode(request.getToStation());
        if (fromStation == null || toStation == null) {
            throw new BizException(ErrorCode.STATION_NOT_FOUND);
        }

        List<TicketInventory> inventories = inventoryDomainService.getInventories(
                train.getId(), request.getTravelDate(), request.getFromStation(), request.getToStation());

        List<TicketPrice> prices = ticketPriceMapper.selectByTrainAndRoute(
                train.getId(), request.getFromStation(), request.getToStation());

        List<SeatType> seatTypes = seatTypeMapper.selectByTrainId(train.getId());

        List<SeatTypeTicketVO> seatTickets = buildSeatTickets(inventories, prices, seatTypes);

        return TicketQueryVO.builder()
                .trainId(train.getId())
                .trainNo(train.getTrainNo())
                .trainType(train.getTrainType())
                .trainTypeName(TRAIN_TYPE_NAMES.getOrDefault(train.getTrainType(), "未知"))
                .fromStation(request.getFromStation())
                .fromStationName(fromStation.getName())
                .toStation(request.getToStation())
                .toStationName(toStation.getName())
                .travelDate(request.getTravelDate())
                .departureTime(train.getDepartureTime())
                .arrivalTime(train.getArrivalTime())
                .runDays(train.getRunDays())
                .mileage(train.getTotalMileage())
                .seatTickets(seatTickets)
                .build();
    }

    private List<SeatTypeTicketVO> buildSeatTickets(List<TicketInventory> inventories,
                                                     List<TicketPrice> prices,
                                                     List<SeatType> seatTypes) {
        Map<String, TicketInventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(TicketInventory::getSeatTypeCode, i -> i));

        Map<String, TicketPrice> priceMap = prices.stream()
                .collect(Collectors.toMap(TicketPrice::getSeatTypeCode, p -> p));

        Map<String, SeatType> seatTypeMap = seatTypes.stream()
                .collect(Collectors.toMap(SeatType::getSeatTypeCode, s -> s));

        List<SeatTypeTicketVO> result = new ArrayList<>();
        for (SeatType seatType : seatTypes) {
            String code = seatType.getSeatTypeCode();
            if (seatType.getTotalCount() == 0) continue;

            TicketInventory inventory = inventoryMap.get(code);
            TicketPrice price = priceMap.get(code);

            int totalCount = inventory != null ? inventory.getTotalCount() : 0;
            int soldCount = inventory != null ? inventory.getSoldCount() : 0;
            int lockedCount = inventory != null ? inventory.getLockedCount() : 0;
            int remainCount = totalCount - soldCount - lockedCount;

            result.add(SeatTypeTicketVO.builder()
                    .seatTypeCode(code)
                    .seatTypeName(seatType.getSeatTypeName())
                    .price(price != null ? price.getPrice() : BigDecimal.ZERO)
                    .totalCount(totalCount)
                    .soldCount(soldCount)
                    .lockedCount(lockedCount)
                    .remainCount(Math.max(0, remainCount))
                    .build());
        }

        return result;
    }

    private String buildCacheKey(TicketQueryRequest request) {
        return CACHE_KEY_PREFIX + request.getTrainNo() + ":" + request.getTravelDate() + ":" + request.getFromStation() + ":" + request.getToStation();
    }

    private String serialize(TicketQueryVO vo) {
        try {
            return objectMapper.writeValueAsString(vo);
        } catch (JsonProcessingException e) {
            log.warn("序列化失败: {}", e.getMessage());
            return vo.toString();
        }
    }

    private TicketQueryVO deserialize(String json) {
        try {
            return objectMapper.readValue(json, TicketQueryVO.class);
        } catch (JsonProcessingException e) {
            log.warn("反序列化失败: {}", e.getMessage());
            return null;
        }
    }
}
