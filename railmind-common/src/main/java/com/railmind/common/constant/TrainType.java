package com.railmind.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TrainType {

    G("G", "高铁", 0.46),
    D("D", "动车", 0.40),
    C("C", "城际", 0.40),
    Z("Z", "直达", 0.18),
    T("T", "特快", 0.15),
    K("K", "快速", 0.12),
    L("L", "临客", 0.10);

    private final String code;
    private final String name;
    private final double basePricePerKm;

    public static TrainType fromCode(String code) {
        for (TrainType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的列车类型: " + code);
    }
}
