package com.railmind.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Schema(description = "创建订单请求")
public class OrderCreateRequest {

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @NotNull(message = "车次ID不能为空")
    @Schema(description = "车次ID", example = "1")
    private Long trainId;

    @NotBlank(message = "车次号不能为空")
    @Schema(description = "车次号", example = "G1")
    private String trainNo;

    @NotNull(message = "乘车日期不能为空")
    @Schema(description = "乘车日期", example = "2026-07-10")
    private LocalDate travelDate;

    @NotBlank(message = "出发站不能为空")
    @Schema(description = "出发站编码", example = "BJP")
    private String fromStation;

    @NotBlank(message = "出发站名不能为空")
    @Schema(description = "出发站名", example = "北京南")
    private String fromStationName;

    @NotBlank(message = "到达站不能为空")
    @Schema(description = "到达站编码", example = "SHH")
    private String toStation;

    @NotBlank(message = "到达站名不能为空")
    @Schema(description = "到达站名", example = "上海虹桥")
    private String toStationName;

    @NotNull(message = "发车时间不能为空")
    @Schema(description = "发车时间", example = "09:00")
    private LocalTime departureTime;

    @NotNull(message = "到达时间不能为空")
    @Schema(description = "到达时间", example = "13:28")
    private LocalTime arrivalTime;

    @NotEmpty(message = "乘车人不能为空")
    @Valid
    @Schema(description = "乘车人列表")
    private List<PassengerInfo> passengers;

    @Data
    @Schema(description = "乘车人信息")
    public static class PassengerInfo {

        @NotNull(message = "乘车人ID不能为空")
        @Schema(description = "乘车人ID", example = "1")
        private Long passengerId;

        @NotBlank(message = "乘车人姓名不能为空")
        @Schema(description = "乘车人姓名", example = "张三")
        private String passengerName;

        @NotBlank(message = "身份证号不能为空")
        @Schema(description = "身份证号(加密)", example = "encrypted_value")
        private String idCard;

        @Schema(description = "身份证号哈希", example = "hash_value")
        private String idCardHash;

        @NotBlank(message = "座位类型不能为空")
        @Schema(description = "座位类型编码", example = "ZE")
        private String seatTypeCode;

        @Schema(description = "座位类型名称", example = "二等座")
        private String seatTypeName;

        @Schema(description = "指定座位号", example = "05车12A")
        private String seatNo;

        @NotNull(message = "票价不能为空")
        @Schema(description = "票价", example = "553.00")
        private BigDecimal ticketPrice;
    }
}
