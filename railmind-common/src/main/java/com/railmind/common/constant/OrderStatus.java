package com.railmind.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {

    CREATED("CREATED", "待支付"),
    LOCKED("LOCKED", "已锁定"),
    PAYING("PAYING", "支付中"),
    PAID("PAID", "已支付"),
    TICKETED("TICKETED", "已出票"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    REFUNDING("REFUNDING", "退款中"),
    REFUNDED("REFUNDED", "已退款");

    private final String code;
    private final String desc;

    public static OrderStatus fromCode(String code) {
        for (OrderStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的订单状态: " + code);
    }
}
