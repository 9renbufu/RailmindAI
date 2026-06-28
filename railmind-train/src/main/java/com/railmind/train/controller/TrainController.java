package com.railmind.train.controller;

import com.railmind.common.model.PageResult;
import com.railmind.common.model.Result;
import com.railmind.train.dto.PriceQueryRequest;
import com.railmind.train.dto.TrainQueryRequest;
import com.railmind.train.service.PriceEngineService;
import com.railmind.train.service.TrainService;
import com.railmind.train.vo.PriceVO;
import com.railmind.train.vo.TrainVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/train")
@RequiredArgsConstructor
@Tag(name = "车次管理", description = "车次查询接口")
public class TrainController {

    private final TrainService trainService;
    private final PriceEngineService priceEngineService;

    @GetMapping("/list")
    @Operation(summary = "车次列表", description = "分页查询车次列表")
    public Result<PageResult<TrainVO>> queryTrains(@Valid TrainQueryRequest request) {
        PageResult<TrainVO> result = trainService.queryTrains(request);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "车次详情", description = "查询车次详情(含途经站和座位类型)")
    public Result<TrainVO> getTrainDetail(
            @Parameter(description = "车次ID")
            @PathVariable Long id) {
        TrainVO train = trainService.getTrainDetail(id);
        return Result.ok(train);
    }

    @GetMapping("/by-no/{trainNo}")
    @Operation(summary = "根据车次号查询", description = "根据车次号查询车次详情")
    public Result<TrainVO> getTrainByNo(
            @Parameter(description = "车次号", example = "G1")
            @PathVariable String trainNo) {
        TrainVO train = trainService.getTrainByNo(trainNo);
        return Result.ok(train);
    }

    @GetMapping("/{id}/stations")
    @Operation(summary = "车次途经站", description = "查询车次的途经站列表")
    public Result<TrainVO> getTrainStations(
            @Parameter(description = "车次ID")
            @PathVariable Long id) {
        TrainVO train = trainService.getTrainDetail(id);
        return Result.ok(train);
    }

    @GetMapping("/price")
    @Operation(summary = "查询票价", description = "查询指定车次区间的票价")
    public Result<PriceVO> queryPrice(@Valid PriceQueryRequest request) {
        PriceVO price = priceEngineService.queryPrice(
                request.getTrainNo(),
                request.getFromStation(),
                request.getToStation());
        return Result.ok(price);
    }
}
