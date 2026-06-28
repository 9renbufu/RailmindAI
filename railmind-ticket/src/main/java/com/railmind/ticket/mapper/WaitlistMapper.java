package com.railmind.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.ticket.domain.model.Waitlist;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface WaitlistMapper extends BaseMapper<Waitlist> {

    @Select("SELECT * FROM t_waitlist WHERE train_id = #{trainId} AND travel_date = #{travelDate} AND from_station = #{fromStation} AND to_station = #{toStation} AND seat_type_code = #{seatTypeCode} AND status = 'WAITING' AND expire_at > NOW() ORDER BY priority DESC, created_at ASC")
    List<Waitlist> selectByRoute(@Param("trainId") Long trainId,
                                 @Param("travelDate") LocalDate travelDate,
                                 @Param("fromStation") String fromStation,
                                 @Param("toStation") String toStation,
                                 @Param("seatTypeCode") String seatTypeCode);

    @Select("SELECT * FROM t_waitlist WHERE user_id = #{userId} AND status = 'WAITING' ORDER BY created_at DESC")
    List<Waitlist> selectUserWaitlist(@Param("userId") Long userId);

    @Update("UPDATE t_waitlist SET status = #{status} WHERE id = #{id} AND status = 'WAITING'")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE t_waitlist SET status = 'CANCELLED' WHERE status = 'WAITING' AND expire_at < NOW()")
    int cancelExpiredWaitlist();

    @Select("SELECT COUNT(*) FROM t_waitlist WHERE train_id = #{trainId} AND travel_date = #{travelDate} AND from_station = #{fromStation} AND to_station = #{toStation} AND seat_type_code = #{seatTypeCode} AND status = 'WAITING' AND priority > #{priority}")
    int countHigherPriority(@Param("trainId") Long trainId,
                            @Param("travelDate") LocalDate travelDate,
                            @Param("fromStation") String fromStation,
                            @Param("toStation") String toStation,
                            @Param("seatTypeCode") String seatTypeCode,
                            @Param("priority") Integer priority);

    @Select("SELECT * FROM t_waitlist WHERE train_id = #{trainId} AND travel_date = #{travelDate} AND from_station = #{fromStation} AND to_station = #{toStation} AND seat_type_code = #{seatTypeCode} AND status = 'WAITING' AND expire_at > NOW() ORDER BY priority DESC, created_at ASC LIMIT #{limit}")
    List<Waitlist> selectTopWaitlist(@Param("trainId") Long trainId,
                                     @Param("travelDate") LocalDate travelDate,
                                     @Param("fromStation") String fromStation,
                                     @Param("toStation") String toStation,
                                     @Param("seatTypeCode") String seatTypeCode,
                                     @Param("limit") int limit);
}
