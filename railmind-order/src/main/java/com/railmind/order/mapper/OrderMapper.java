package com.railmind.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.order.domain.model.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM t_order WHERE order_no = #{orderNo} AND deleted = 0")
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("SELECT * FROM t_order WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<Order> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM t_order WHERE status = #{status} AND deleted = 0")
    List<Order> selectByStatus(@Param("status") String status);

    @Select("SELECT * FROM t_order WHERE status = 'CREATED' AND pay_deadline < #{now} AND deleted = 0")
    List<Order> selectExpiredOrders(@Param("now") LocalDateTime now);

    @Update("UPDATE t_order SET status = #{newStatus}, updated_at = NOW() WHERE order_no = #{orderNo} AND status = #{expectedStatus}")
    int updateStatus(@Param("orderNo") String orderNo,
                     @Param("expectedStatus") String expectedStatus,
                     @Param("newStatus") String newStatus);

    @Update("UPDATE t_order SET status = 'CANCELLED', cancel_reason = #{reason}, cancelled_at = NOW(), updated_at = NOW() WHERE order_no = #{orderNo} AND status = 'CREATED'")
    int cancelOrder(@Param("orderNo") String orderNo, @Param("reason") String reason);

    @Update("UPDATE t_order SET status = 'PAID', paid_at = NOW(), updated_at = NOW() WHERE order_no = #{orderNo} AND status = 'CREATED'")
    int payOrder(@Param("orderNo") String orderNo);
}
