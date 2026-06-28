package com.railmind.train.service;

import com.railmind.train.vo.PriceVO;

public interface PriceEngineService {

    /**
     * 查询区间票价
     */
    PriceVO queryPrice(String trainNo, String fromStation, String toStation);
}
