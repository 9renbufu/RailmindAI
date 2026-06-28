package com.railmind.train.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.common.model.PageResult;
import com.railmind.train.domain.model.SeatType;
import com.railmind.train.domain.model.Station;
import com.railmind.train.domain.model.Train;
import com.railmind.train.domain.model.TrainStation;
import com.railmind.train.dto.TrainQueryRequest;
import com.railmind.train.mapper.SeatTypeMapper;
import com.railmind.train.mapper.StationMapper;
import com.railmind.train.mapper.TrainMapper;
import com.railmind.train.mapper.TrainStationMapper;
import com.railmind.train.service.TrainService;
import com.railmind.train.vo.SeatTypeVO;
import com.railmind.train.vo.TrainStationVO;
import com.railmind.train.vo.TrainVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainServiceImpl implements TrainService {

    private final TrainMapper trainMapper;
    private final TrainStationMapper trainStationMapper;
    private final SeatTypeMapper seatTypeMapper;
    private final StationMapper stationMapper;

    private static final Map<String, String> TRAIN_TYPE_NAMES = Map.of(
            "G", "高铁",
            "D", "动车",
            "C", "城际",
            "Z", "直达",
            "T", "特快",
            "K", "快速",
            "L", "临客"
    );

    @Override
    public PageResult<TrainVO> queryTrains(TrainQueryRequest request) {
        log.info("分页查询车次: {}", request);

        LambdaQueryWrapper<Train> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Train::getStatus, 1);

        if (StringUtils.hasText(request.getTrainNo())) {
            wrapper.like(Train::getTrainNo, request.getTrainNo());
        }
        if (StringUtils.hasText(request.getTrainType())) {
            wrapper.eq(Train::getTrainType, request.getTrainType());
        }
        if (StringUtils.hasText(request.getStartStation())) {
            wrapper.eq(Train::getStartStation, request.getStartStation());
        }
        if (StringUtils.hasText(request.getEndStation())) {
            wrapper.eq(Train::getEndStation, request.getEndStation());
        }

        wrapper.orderByAsc(Train::getTrainNo);

        Page<Train> page = new Page<>(request.getPageNum(), request.getPageSize());
        Page<Train> result = trainMapper.selectPage(page, wrapper);

        List<TrainVO> trainVOs = result.getRecords().stream()
                .map(this::toTrainVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), request.getPageNum(), request.getPageSize(), trainVOs);
    }

    @Override
    public TrainVO getTrainDetail(Long trainId) {
        log.info("查询车次详情: trainId={}", trainId);

        Train train = trainMapper.selectById(trainId);
        if (train == null) {
            throw new BizException(ErrorCode.TRAIN_NOT_FOUND);
        }

        TrainVO trainVO = toTrainVO(train);

        // 查询途经站
        List<TrainStation> stations = trainStationMapper.selectByTrainId(trainId);
        List<TrainStationVO> stationVOs = stations.stream()
                .map(ts -> {
                    Station station = stationMapper.selectByCode(ts.getStationCode());
                    return TrainStationVO.builder()
                            .id(ts.getId())
                            .stationCode(ts.getStationCode())
                            .stationName(station != null ? station.getName() : null)
                            .stationOrder(ts.getStationOrder())
                            .arrivalTime(ts.getArrivalTime())
                            .departureTime(ts.getDepartureTime())
                            .stopDuration(ts.getStopDuration())
                            .mileage(ts.getMileage())
                            .build();
                })
                .collect(Collectors.toList());
        trainVO.setStations(stationVOs);

        // 查询座位类型
        List<SeatType> seatTypes = seatTypeMapper.selectByTrainId(trainId);
        List<SeatTypeVO> seatTypeVOs = seatTypes.stream()
                .map(st -> SeatTypeVO.builder()
                        .seatTypeCode(st.getSeatTypeCode())
                        .seatTypeName(st.getSeatTypeName())
                        .totalCount(st.getTotalCount())
                        .priceFactor(st.getPriceFactor())
                        .build())
                .collect(Collectors.toList());
        trainVO.setSeatTypes(seatTypeVOs);

        return trainVO;
    }

    @Override
    public TrainVO getTrainByNo(String trainNo) {
        log.info("根据车次号查询: trainNo={}", trainNo);

        Train train = trainMapper.selectByTrainNo(trainNo);
        if (train == null) {
            throw new BizException(ErrorCode.TRAIN_NOT_FOUND);
        }

        return getTrainDetail(train.getId());
    }

    private TrainVO toTrainVO(Train train) {
        Station startStation = stationMapper.selectByCode(train.getStartStation());
        Station endStation = stationMapper.selectByCode(train.getEndStation());

        return TrainVO.builder()
                .id(train.getId())
                .trainNo(train.getTrainNo())
                .trainType(train.getTrainType())
                .trainTypeName(TRAIN_TYPE_NAMES.getOrDefault(train.getTrainType(), "未知"))
                .startStation(train.getStartStation())
                .startStationName(startStation != null ? startStation.getName() : null)
                .endStation(train.getEndStation())
                .endStationName(endStation != null ? endStation.getName() : null)
                .departureTime(train.getDepartureTime())
                .arrivalTime(train.getArrivalTime())
                .runDays(train.getRunDays())
                .totalMileage(train.getTotalMileage())
                .status(train.getStatus())
                .build();
    }
}
