package com.railmind.order.service;

import com.railmind.common.model.PageResult;
import com.railmind.order.dto.OrderCreateRequest;
import com.railmind.order.dto.OrderQueryRequest;
import com.railmind.order.vo.OrderVO;

public interface OrderService {

    OrderVO createOrder(OrderCreateRequest request);

    OrderVO getOrderDetail(String orderNo);

    PageResult<OrderVO> queryOrders(OrderQueryRequest request);

    OrderVO cancelOrder(String orderNo, Long userId, String reason);

    String getOrderStatus(String orderNo);

    void handleExpiredOrders();
}
