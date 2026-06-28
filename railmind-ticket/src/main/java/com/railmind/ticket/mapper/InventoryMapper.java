package com.railmind.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.ticket.domain.model.TicketInventory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface InventoryMapper extends BaseMapper<TicketInventory> {

    @Select("SELECT * FROM t_ticket_inventory WHERE train_id = #{trainId} AND travel_date = #{travelDate} AND from_station = #{fromStation} AND to_station = #{toStation} AND seat_type_code = #{seatTypeCode}")
    TicketInventory selectByUniqueKey(@Param("trainId") Long trainId,
                                       @Param("travelDate") LocalDate travelDate,
                                       @Param("fromStation") String fromStation,
                                       @Param("toStation") String toStation,
                                       @Param("seatTypeCode") String seatTypeCode);

    @Select("SELECT * FROM t_ticket_inventory WHERE train_id = #{trainId} AND travel_date = #{travelDate} AND from_station = #{fromStation} AND to_station = #{toStation}")
    List<TicketInventory> selectByTrainAndRoute(@Param("trainId") Long trainId,
                                                 @Param("travelDate") LocalDate travelDate,
                                                 @Param("fromStation") String fromStation,
                                                 @Param("toStation") String toStation);

    @Select("SELECT * FROM t_ticket_inventory WHERE train_id = #{trainId} AND travel_date = #{travelDate}")
    List<TicketInventory> selectByTrainAndDate(@Param("trainId") Long trainId,
                                                @Param("travelDate") LocalDate travelDate);

    @Update("UPDATE t_ticket_inventory SET sold_count = sold_count + #{count}, version = version + 1 WHERE id = #{id} AND (total_count - sold_count - locked_count) >= #{count} AND version = #{version}")
    int deductStock(@Param("id") Long id, @Param("count") int count, @Param("version") Long version);

    @Update("UPDATE t_ticket_inventory SET sold_count = sold_count - #{count}, version = version + 1 WHERE id = #{id}")
    int rollbackStock(@Param("id") Long id, @Param("count") int count);

    @Update("UPDATE t_ticket_inventory SET locked_count = locked_count + #{count}, version = version + 1 WHERE id = #{id} AND (total_count - sold_count - locked_count) >= #{count} AND version = #{version}")
    int lockStock(@Param("id") Long id, @Param("count") int count, @Param("version") Long version);

    @Update("UPDATE t_ticket_inventory SET locked_count = locked_count - #{count}, version = version + 1 WHERE id = #{id}")
    int unlockStock(@Param("id") Long id, @Param("count") int count);

    @Select("SELECT * FROM t_ticket_inventory WHERE travel_date = #{travelDate}")
    List<TicketInventory> selectByTravelDate(@Param("travelDate") LocalDate travelDate);
}
