package com.railmind.ticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.ticket.domain.model.SeatLock;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface SeatLockMapper extends BaseMapper<SeatLock> {

    @Select("SELECT * FROM t_seat_lock WHERE train_id = #{trainId} AND travel_date = #{travelDate} AND seat_type_code = #{seatTypeCode} AND seat_no = #{seatNo} AND status = 1 AND expire_time > NOW()")
    SeatLock selectActiveLock(@Param("trainId") Long trainId,
                              @Param("travelDate") LocalDate travelDate,
                              @Param("seatTypeCode") String seatTypeCode,
                              @Param("seatNo") String seatNo);

    @Select("SELECT seat_no FROM t_seat_lock WHERE train_id = #{trainId} AND travel_date = #{travelDate} AND seat_type_code = #{seatTypeCode} AND status = 1 AND expire_time > NOW()")
    List<String> selectLockedSeats(@Param("trainId") Long trainId,
                                   @Param("travelDate") LocalDate travelDate,
                                   @Param("seatTypeCode") String seatTypeCode);

    @Update("UPDATE t_seat_lock SET status = 0 WHERE id = #{id} AND status = 1")
    int releaseLock(@Param("id") Long id);

    @Update("UPDATE t_seat_lock SET status = 0 WHERE order_no = #{orderNo} AND status = 1")
    int releaseLockByOrderNo(@Param("orderNo") String orderNo);

    @Update("UPDATE t_seat_lock SET status = 0 WHERE expire_time < NOW() AND status = 1")
    int releaseExpiredLocks();

    @Select("SELECT * FROM t_seat_lock WHERE user_id = #{userId} AND status = 1 AND expire_time > NOW()")
    List<SeatLock> selectUserActiveLocks(@Param("userId") Long userId);

    @Select("SELECT * FROM t_seat_lock WHERE order_no = #{orderNo} AND status = 1")
    List<SeatLock> selectByOrderNo(@Param("orderNo") String orderNo);
}
