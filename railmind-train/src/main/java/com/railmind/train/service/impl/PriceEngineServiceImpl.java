package com.railmind.train.service.impl;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.train.domain.model.Station;
import com.railmind.train.domain.model.TicketPrice;
import com.railmind.train.domain.model.Train;
import com.railmind.train.domain.model.TrainStation;
import com.railmind.train.mapper.StationMapper;
import com.railmind.train.mapper.TicketPriceMapper;
import com.railmind.train.mapper.TrainMapper;
import com.railmind.train.mapper.TrainStationMapper;
import com.railmind.train.service.PriceEngineService;
import com.railmind.train.vo.PriceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceEngineServiceImpl implements PriceEngineService {

    private final TrainMapper trainMapper;
    private final StationMapper stationMapper;
    private final TrainStationMapper trainStationMapper;
    private final TicketPriceMapper ticketPriceMapper;

    @Override
    public PriceVO queryPrice(String trainNo, String fromStation, String toStation) {
        log.info("查询票价: trainNo={}, from={}, to={}", trainNo, fromStation, toStation);

        // 查询车次
        Train train = trainMapper.selectByTrainNo(trainNo);
        if (train == null) {
            throw new BizException(ErrorCode.TRAIN_NOT_FOUND);
        }

        // 查询站点信息
        Station from = stationMapper.selectByCode(fromStation);
        if (from == null) {
            throw new BizException(ErrorCode.STATION_NOT_FOUND);
        }
        Station to = stationMapper.selectByCode(toStation);
        if (to == null) {
            throw new BizException(ErrorCode.STATION_NOT_FOUND);
        }

        // 查询区间里程
        TrainStation fromTS = trainStationMapper.selectByTrainIdAndStationCode(train.getId(), fromStation);
        TrainStation toTS = trainStationMapper.selectByTrainIdAndStationCode(train.getId(), toStation);

        Integer mileage = null;
        if (fromTS != null && toTS != null) {
            mileage = toTS.getMileage() - fromTS.getMileage();
        }

        // 查询票价
        List<TicketPrice> prices = ticketPriceMapper.selectByTrainAndRoute(train.getId(), fromStation, toStation);

        List<PriceVO.SeatPriceVO> seatPrices = prices.stream()
                .map(p -> PriceVO.SeatPriceVO.builder()
                        .seatTypeCode(p.getSeatTypeCode())
                        .seatTypeName(getSeatTypeName(p.getSeatTypeCode()))
                        .price(p.getPrice())
                        .build())
                .collect(Collectors.toList());

        return PriceVO.builder()
                .trainNo(trainNo)
                .fromStation(fromStation)
                .fromStationName(from.getName())
                .toStation(toStation)
                .toStationName(to.getName())
                .mileage(mileage)
                .seatPrices(seatPrices)
                .build();
    }

    private String getSeatTypeName(String seatTypeCode) {
        return switch (seatTypeCode) {
            case "SW" -> "商务座";
            case "ZY" -> "一等座";
            case "ZE" -> "二等座";
            case "RW" -> "软卧";
            case "YW" -> "硬卧";
            case "RZ" -> "软座";
            case "YZ" -> "硬座";
            default -> seatTypeCode;
        };
    }
}
