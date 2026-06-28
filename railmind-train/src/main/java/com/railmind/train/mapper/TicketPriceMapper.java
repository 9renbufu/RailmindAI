package com.railmind.train.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.train.domain.model.TicketPrice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TicketPriceMapper extends BaseMapper<TicketPrice> {

    /**
     * 查询指定车次、区间的票价
     */
    @Select("SELECT * FROM t_ticket_price WHERE train_id = #{trainId} AND from_station = #{fromStation} AND to_station = #{toStation}")
    List<TicketPrice> selectByTrainAndRoute(@Param("trainId") Long trainId, @Param("fromStation") String fromStation, @Param("toStation") String toStation);

    /**
     * 查询指定车次、区间、座位类型的票价
     */
    @Select("SELECT * FROM t_ticket_price WHERE train_id = #{trainId} AND from_station = #{fromStation} AND to_station = #{toStation} AND seat_type_code = #{seatTypeCode}")
    TicketPrice selectByTrainRouteAndSeatType(@Param("trainId") Long trainId, @Param("fromStation") String fromStation, @Param("toStation") String toStation, @Param("seatTypeCode") String seatTypeCode);
}
