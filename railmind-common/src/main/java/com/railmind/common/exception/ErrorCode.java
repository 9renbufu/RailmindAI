package com.railmind.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户模块 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PHONE_ALREADY_EXISTS(1003, "手机号已注册"),
    PASSWORD_ERROR(1004, "密码错误"),
    USER_DISABLED(1005, "账号已禁用"),
    USER_FROZEN(1006, "账号已冻结"),
    TOKEN_EXPIRED(1007, "Token已过期"),
    TOKEN_INVALID(1008, "Token无效"),
    CAPTCHA_ERROR(1009, "验证码错误"),
    CAPTCHA_EXPIRED(1010, "验证码已过期"),
    LOGIN_FAIL_LIMIT(1011, "登录失败次数过多，请30分钟后重试"),
    PASSENGER_LIMIT(1012, "乘车人数量已达上限(最多15人)"),
    PASSENGER_ALREADY_EXISTS(1013, "该乘车人已存在"),
    ID_CARD_INVALID(1014, "身份证号格式不正确"),

    // 车次模块 2xxx
    TRAIN_NOT_FOUND(2001, "车次不存在"),
    STATION_NOT_FOUND(2002, "站点不存在"),
    TRAIN_STOPPED(2003, "车次已停运"),
    SCHEDULE_NOT_FOUND(2004, "运行计划不存在"),
    PRICE_NOT_FOUND(2005, "票价信息不存在"),

    // 票务模块 3xxx
    TICKET_NOT_ENOUGH(3001, "余票不足"),
    SEAT_ALREADY_LOCKED(3002, "座位已被占用"),
    SEAT_LOCK_EXPIRED(3003, "座位锁定已过期"),
    SEAT_LOCK_NOT_FOUND(3004, "座位锁定记录不存在"),
    INVENTORY_NOT_FOUND(3005, "库存记录不存在"),
    WAITLIST_ALREADY_EXISTS(3006, "已加入候补队列"),
    WAITLIST_NOT_FOUND(3007, "候补记录不存在"),
    WAITLIST_EXPIRED(3008, "候补已过期"),

    // 订单模块 4xxx
    ORDER_NOT_FOUND(4001, "订单不存在"),
    ORDER_STATUS_ERROR(4002, "订单状态不允许此操作"),
    ORDER_ALREADY_PAID(4003, "订单已支付"),
    ORDER_EXPIRED(4004, "订单已过期"),
    ORDER_DUPLICATE(4005, "请勿重复下单"),
    REFUND_TIME_PASSED(4006, "已超过退票时间"),
    REFUND_FEE_ERROR(4007, "退票费计算异常"),
    CHANGE_NOT_ALLOWED(4008, "当前订单不允许改签"),
    CHANGE_NO_ALTERNATIVE(4009, "没有可改签的车次"),

    // 支付模块 5xxx
    PAYMENT_NOT_FOUND(5001, "支付单不存在"),
    PAYMENT_ALREADY_DONE(5002, "已支付成功"),
    PAYMENT_EXPIRED(5003, "支付已过期"),
    PAYMENT_AMOUNT_ERROR(5004, "支付金额不一致"),
    REFUND_NOT_ALLOWED(5005, "当前状态不允许退款"),

    // AI模块 6xxx
    AI_SERVICE_ERROR(6001, "AI服务暂时不可用"),
    AI_SESSION_NOT_FOUND(6002, "会话不存在"),
    AI_RATE_LIMIT(6003, "AI调用频率超限"),

    // 限流 9xxx
    RATE_LIMIT_EXCEEDED(9001, "请求过于频繁，请稍后重试"),
    ;

    private final int code;
    private final String message;
}
