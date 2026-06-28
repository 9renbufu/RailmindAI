package com.railmind.train.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.train.domain.model.SeatType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SeatTypeMapper extends BaseMapper<SeatType> {

    /**
     * 根据车次ID查询座位类型列表
     */
    @Select("SELECT * FROM t_seat_type WHERE train_id = #{trainId}")
    List<SeatType> selectByTrainId(@Param("trainId") Long trainId);

    /**
     * 根据车次ID和座位编码查询
     */
    @Select("SELECT * FROM t_seat_type WHERE train_id = #{trainId} AND seat_type_code = #{seatTypeCode}")
    SeatType selectByTrainIdAndCode(@Param("trainId") Long trainId, @Param("seatTypeCode") String seatTypeCode);
}
