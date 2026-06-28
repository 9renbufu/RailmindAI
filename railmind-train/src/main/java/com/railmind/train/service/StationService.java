package com.railmind.train.service;

import com.railmind.train.vo.StationVO;

import java.util.List;

public interface StationService {

    /**
     * 模糊搜索站点（支持中文名、城市名、编码）
     */
    List<StationVO> searchStations(String keyword);

    /**
     * 根据编码查询站点详情
     */
    StationVO getStationByCode(String code);

    /**
     * 根据城市查询站点列表
     */
    List<StationVO> getStationsByCity(String city);
}
