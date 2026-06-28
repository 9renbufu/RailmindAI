package com.railmind.train.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.train.domain.model.Train;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TrainMapper extends BaseMapper<Train> {

    /**
     * 根据车次号查询
     */
    @Select("SELECT * FROM t_train WHERE train_no = #{trainNo} AND deleted = 0")
    Train selectByTrainNo(@Param("trainNo") String trainNo);

    /**
     * 根据始发站和终到站查询车次
     */
    @Select("SELECT * FROM t_train WHERE start_station = #{startStation} AND end_station = #{endStation} AND status = 1 AND deleted = 0")
    List<Train> selectByStartAndEnd(@Param("startStation") String startStation, @Param("endStation") String endStation);

    /**
     * 查询所有正常运行的车次
     */
    @Select("SELECT * FROM t_train WHERE status = 1 AND deleted = 0 ORDER BY train_no")
    List<Train> selectAllActive();
}
