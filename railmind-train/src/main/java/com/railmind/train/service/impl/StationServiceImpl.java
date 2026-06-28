package com.railmind.train.service.impl;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.train.domain.model.Station;
import com.railmind.train.mapper.StationMapper;
import com.railmind.train.service.StationService;
import com.railmind.train.vo.StationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationMapper stationMapper;

    @Override
    public List<StationVO> searchStations(String keyword) {
        log.info("搜索站点: keyword={}", keyword);
        List<Station> stations = stationMapper.searchByKeyword(keyword);
        return stations.stream()
                .map(this::toStationVO)
                .collect(Collectors.toList());
    }

    @Override
    public StationVO getStationByCode(String code) {
        log.info("查询站点详情: code={}", code);
        Station station = stationMapper.selectByCode(code);
        if (station == null) {
            throw new BizException(ErrorCode.STATION_NOT_FOUND);
        }
        return toStationVO(station);
    }

    @Override
    public List<StationVO> getStationsByCity(String city) {
        log.info("查询城市站点: city={}", city);
        List<Station> stations = stationMapper.selectByCity(city);
        return stations.stream()
                .map(this::toStationVO)
                .collect(Collectors.toList());
    }

    private StationVO toStationVO(Station station) {
        return StationVO.builder()
                .id(station.getId())
                .code(station.getCode())
                .name(station.getName())
                .city(station.getCity())
                .province(station.getProvince())
                .longitude(station.getLongitude())
                .latitude(station.getLatitude())
                .bureau(station.getBureau())
                .build();
    }
}
