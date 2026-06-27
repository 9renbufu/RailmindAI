package com.railmind.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeatType {

    SW("SW", "商务座", 3.0),
    YW("YW", "一等座", 1.8),
    ZE("ZE", "二等座", 1.0),
    RZ("RZ", "软座", 1.5),
    YZ("YZ", "硬座", 0.5),
    RW("RW", "软卧", 2.0),
    YW_SLEEPER("YW_SLEEPER", "硬卧", 1.2);

    private final String code;
    private final String name;
    private final double priceFactor;

    public static SeatType fromCode(String code) {
        for (SeatType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的座位类型: " + code);
    }
}
