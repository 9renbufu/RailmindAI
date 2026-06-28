package com.railmind.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.railmind.common.exception.BizException;
import com.railmind.common.model.PageResult;
import com.railmind.order.domain.model.Order;
import com.railmind.order.domain.model.OrderItem;
import com.railmind.order.domain.service.OrderDomainService;
import com.railmind.order.dto.OrderCreateRequest;
import com.railmind.order.dto.OrderQueryRequest;
import com.railmind.order.mapper.OrderItemMapper;
import com.railmind.order.mapper.OrderMapper;
import com.railmind.order.producer.OrderEventProducer;
import com.railmind.order.service.impl.OrderServiceImpl;
import com.railmind.order.vo.OrderVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private OrderDomainService orderDomainService;

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private OrderItem testOrderItem;
    private OrderCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        testOrder = Order.builder()
                .id(1L)
                .orderNo("123456789012345678")
                .userId(1L)
                .trainId(1L)
                .trainNo("G1")
                .travelDate(LocalDate.of(2026, 7, 10))
                .fromStation("BJP")
                .fromStationName("北京南")
                .toStation("SHH")
                .toStationName("上海虹桥")
                .departureTime(LocalTime.of(9, 0))
                .arrivalTime(LocalTime.of(13, 28))
                .totalAmount(new BigDecimal("553.00"))
                .status("CREATED")
                .payDeadline(LocalDateTime.now().plusMinutes(15))
                .createdAt(LocalDateTime.now())
                .deleted(0)
                .build();

        testOrderItem = OrderItem.builder()
                .id(1L)
                .orderId(1L)
                .orderNo("123456789012345678")
                .passengerId(1L)
                .passengerName("张三")
                .seatTypeCode("ZE")
                .seatTypeName("二等座")
                .seatNo("05车12A")
                .ticketPrice(new BigDecimal("553.00"))
                .status("NORMAL")
                .build();

        createRequest = new OrderCreateRequest();
        createRequest.setUserId(1L);
        createRequest.setTrainId(1L);
        createRequest.setTrainNo("G1");
        createRequest.setTravelDate(LocalDate.of(2026, 7, 10));
        createRequest.setFromStation("BJP");
        createRequest.setFromStationName("北京南");
        createRequest.setToStation("SHH");
        createRequest.setToStationName("上海虹桥");
        createRequest.setDepartureTime(LocalTime.of(9, 0));
        createRequest.setArrivalTime(LocalTime.of(13, 28));

        OrderCreateRequest.PassengerInfo passenger = new OrderCreateRequest.PassengerInfo();
        passenger.setPassengerId(1L);
        passenger.setPassengerName("张三");
        passenger.setIdCard("encrypted");
        passenger.setIdCardHash("hash");
        passenger.setSeatTypeCode("ZE");
        passenger.setSeatTypeName("二等座");
        passenger.setSeatNo("05车12A");
        passenger.setTicketPrice(new BigDecimal("553.00"));
        createRequest.setPassengers(Arrays.asList(passenger));
    }

    @Test
    void createOrder_success() {
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(orderDomainService.createOrder(any(OrderCreateRequest.class))).thenReturn(testOrder);
        when(orderDomainService.createOrderItems(any(Order.class), any(OrderCreateRequest.class)))
                .thenReturn(Arrays.asList(testOrderItem));
        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);

        OrderVO result = orderService.createOrder(createRequest);

        assertNotNull(result);
        assertEquals("123456789012345678", result.getOrderNo());
        assertEquals("G1", result.getTrainNo());
        assertEquals("CREATED", result.getStatus());
        verify(orderEventProducer).sendOrderCreatedEvent(any());
    }

    @Test
    void createOrder_duplicate_throwsException() {
        when(orderMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testOrder);

        BizException ex = assertThrows(BizException.class, () -> orderService.createOrder(createRequest));
        assertEquals(4005, ex.getCode());
        assertTrue(ex.getMessage().contains("重复下单"));
    }

    @Test
    void getOrderDetail_success() {
        when(orderMapper.selectByOrderNo("123456789012345678")).thenReturn(testOrder);
        when(orderItemMapper.selectByOrderNo("123456789012345678")).thenReturn(Arrays.asList(testOrderItem));

        OrderVO result = orderService.getOrderDetail("123456789012345678");

        assertNotNull(result);
        assertEquals("123456789012345678", result.getOrderNo());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void getOrderDetail_notFound_throwsException() {
        when(orderMapper.selectByOrderNo("nonexistent")).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> orderService.getOrderDetail("nonexistent"));
        assertEquals(4001, ex.getCode());
    }

    @Test
    void queryOrders_success() {
        Page<Order> page = new Page<>(1, 10);
        page.setRecords(Arrays.asList(testOrder));
        page.setTotal(1);
        when(orderMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);
        when(orderItemMapper.selectByOrderNo("123456789012345678")).thenReturn(Arrays.asList(testOrderItem));

        OrderQueryRequest request = new OrderQueryRequest();
        request.setUserId(1L);
        request.setPageNum(1);
        request.setPageSize(10);

        PageResult<OrderVO> result = orderService.queryOrders(request);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void cancelOrder_success() {
        when(orderMapper.selectByOrderNo("123456789012345678")).thenReturn(testOrder);
        when(orderDomainService.canCancel(testOrder)).thenReturn(true);
        when(orderMapper.cancelOrder("123456789012345678", "用户主动取消")).thenReturn(1);

        Order cancelledOrder = Order.builder()
                .id(1L).orderNo("123456789012345678").userId(1L).status("CANCELLED")
                .cancelReason("用户主动取消").cancelledAt(LocalDateTime.now())
                .build();
        when(orderMapper.selectByOrderNo("123456789012345678"))
                .thenReturn(testOrder)
                .thenReturn(cancelledOrder);
        when(orderItemMapper.selectByOrderNo("123456789012345678")).thenReturn(Arrays.asList(testOrderItem));

        OrderVO result = orderService.cancelOrder("123456789012345678", 1L, "用户主动取消");

        assertNotNull(result);
        assertEquals("CANCELLED", result.getStatus());
    }

    @Test
    void cancelOrder_wrongUser_throwsException() {
        when(orderMapper.selectByOrderNo("123456789012345678")).thenReturn(testOrder);

        BizException ex = assertThrows(BizException.class,
                () -> orderService.cancelOrder("123456789012345678", 999L, "用户主动取消"));
        assertEquals(9998, ex.getCode());
    }

    @Test
    void cancelOrder_wrongStatus_throwsException() {
        Order paidOrder = Order.builder()
                .id(1L).orderNo("123456789012345678").userId(1L).status("PAID").build();
        when(orderMapper.selectByOrderNo("123456789012345678")).thenReturn(paidOrder);
        when(orderDomainService.canCancel(paidOrder)).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> orderService.cancelOrder("123456789012345678", 1L, "用户主动取消"));
        assertEquals(4002, ex.getCode());
    }

    @Test
    void getOrderStatus_success() {
        when(orderMapper.selectByOrderNo("123456789012345678")).thenReturn(testOrder);

        String status = orderService.getOrderStatus("123456789012345678");

        assertEquals("CREATED", status);
    }

    @Test
    void handleExpiredOrders_success() {
        Order expiredOrder = Order.builder()
                .id(2L).orderNo("expired_order").userId(2L).status("CREATED")
                .payDeadline(LocalDateTime.now().minusMinutes(1)).build();
        when(orderMapper.selectExpiredOrders(any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(expiredOrder));
        when(orderMapper.cancelOrder("expired_order", "支付超时自动取消")).thenReturn(1);

        orderService.handleExpiredOrders();

        verify(orderMapper).cancelOrder("expired_order", "支付超时自动取消");
    }
}
