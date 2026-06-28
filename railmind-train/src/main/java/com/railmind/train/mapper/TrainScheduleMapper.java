package com.railmind.train.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.train.domain.model.TrainSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface TrainScheduleMapper extends BaseMapper<TrainSchedule> {

    /**
     * 根据车次ID和日期查询运行计划
     */
    @Select("SELECT * FROM t_train_schedule WHERE train_id = #{trainId} AND travel_date = #{travelDate}")
    TrainSchedule selectByTrainIdAndDate(@Param("trainId") Long trainId, @Param("travelDate") LocalDate travelDate);

    /**
     * 查询某日所有运行的车次
     */
    @Select("SELECT * FROM t_train_schedule WHERE travel_date = #{travelDate} AND status = 1")
    List<TrainSchedule> selectByTravelDate(@Param("travelDate") LocalDate travelDate);

    /**
     * 查询某车次在日期范围内的运行计划
     */
    @Select("SELECT * FROM t_train_schedule WHERE train_id = #{trainId} AND travel_date BETWEEN #{startDate} AND #{endDate} ORDER BY travel_date")
    List<TrainSchedule> selectByTrainIdAndDateRange(@Param("trainId") Long trainId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
