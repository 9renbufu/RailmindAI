package com.railmind.order.service;

public interface OrderStateMachineService {

    boolean canTransition(String currentStatus, String targetStatus);

    void transition(String orderNo, String currentStatus, String targetStatus);

    void lockOrder(String orderNo);

    void payOrder(String orderNo);

    void ticketOrder(String orderNo);

    void completeOrder(String orderNo);

    void cancelOrder(String orderNo, String reason);
}
