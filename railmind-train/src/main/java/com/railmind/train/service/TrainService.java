package com.railmind.train.service;

import com.railmind.common.model.PageResult;
import com.railmind.train.dto.TrainQueryRequest;
import com.railmind.train.vo.TrainVO;

public interface TrainService {

    /**
     * 分页查询车次列表
     */
    PageResult<TrainVO> queryTrains(TrainQueryRequest request);

    /**
     * 查询车次详情（含途经站和座位类型）
     */
    TrainVO getTrainDetail(Long trainId);

    /**
     * 根据车次号查询车次详情
     */
    TrainVO getTrainByNo(String trainNo);
}
