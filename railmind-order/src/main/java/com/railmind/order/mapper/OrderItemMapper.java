package com.railmind.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.railmind.order.domain.model.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("SELECT * FROM t_order_item WHERE order_id = #{orderId}")
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    @Select("SELECT * FROM t_order_item WHERE order_no = #{orderNo}")
    List<OrderItem> selectByOrderNo(@Param("orderNo") String orderNo);
}
