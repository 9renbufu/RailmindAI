package com.railmind.train.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.train.domain.model.TrainStation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TrainStationMapper extends BaseMapper<TrainStation> {

    /**
     * 根据车次ID查询途经站列表（按站序排序）
     */
    @Select("SELECT * FROM t_train_station WHERE train_id = #{trainId} ORDER BY station_order")
    List<TrainStation> selectByTrainId(@Param("trainId") Long trainId);

    /**
     * 根据车次ID和站点编码查询途经站信息
     */
    @Select("SELECT * FROM t_train_station WHERE train_id = #{trainId} AND station_code = #{stationCode}")
    TrainStation selectByTrainIdAndStationCode(@Param("trainId") Long trainId, @Param("stationCode") String stationCode);

    /**
     * 查询两个站点之间的里程差
     */
    @Select("SELECT " +
            "(SELECT mileage FROM t_train_station WHERE train_id = #{trainId} AND station_code = #{toStation}) - " +
            "(SELECT mileage FROM t_train_station WHERE train_id = #{trainId} AND station_code = #{fromStation})")
    Integer getMileageBetween(@Param("trainId") Long trainId, @Param("fromStation") String fromStation, @Param("toStation") String toStation);
}
