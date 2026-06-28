package com.railmind.train.controller;

import com.railmind.common.model.Result;
import com.railmind.train.service.StationService;
import com.railmind.train.vo.StationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/station")
@RequiredArgsConstructor
@Tag(name = "站点管理", description = "站点查询接口")
public class StationController {

    private final StationService stationService;

    @GetMapping("/search")
    @Operation(summary = "搜索站点", description = "根据关键词模糊搜索站点(支持站名/城市/编码)")
    public Result<List<StationVO>> searchStations(
            @Parameter(description = "搜索关键词", example = "北京")
            @RequestParam String keyword) {
        List<StationVO> stations = stationService.searchStations(keyword);
        return Result.ok(stations);
    }

    @GetMapping("/{code}")
    @Operation(summary = "站点详情", description = "根据站点编码查询站点详情")
    public Result<StationVO> getStationByCode(
            @Parameter(description = "站点编码", example = "BJP")
            @PathVariable String code) {
        StationVO station = stationService.getStationByCode(code);
        return Result.ok(station);
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "城市站点", description = "查询指定城市的所有站点")
    public Result<List<StationVO>> getStationsByCity(
            @Parameter(description = "城市名称", example = "北京")
            @PathVariable String city) {
        List<StationVO> stations = stationService.getStationsByCity(city);
        return Result.ok(stations);
    }
}
