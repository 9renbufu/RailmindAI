package com.railmind.ticket.controller;

import com.railmind.common.model.Result;
import com.railmind.ticket.dto.BatchQueryRequest;
import com.railmind.ticket.dto.TicketQueryRequest;
import com.railmind.ticket.service.TicketQueryService;
import com.railmind.ticket.vo.TicketQueryVO;
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
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
@Tag(name = "余票查询", description = "余票查询相关接口")
public class TicketQueryController {

    private final TicketQueryService ticketQueryService;

    @GetMapping("/query")
    @Operation(summary = "余票查询", description = "查询指定车次、日期、区间的各座位类型余票")
    public Result<TicketQueryVO> queryTicket(
            @Valid TicketQueryRequest request) {
        log.info("余票查询请求: trainNo={}, date={}, from={}, to={}",
                request.getTrainNo(), request.getTravelDate(), request.getFromStation(), request.getToStation());
        TicketQueryVO result = ticketQueryService.queryTicket(request);
        return Result.ok(result);
    }

    @PostMapping("/batch-query")
    @Operation(summary = "批量余票查询", description = "同日多车次余票批量查询")
    public Result<List<TicketQueryVO>> batchQueryTicket(
            @Valid @RequestBody BatchQueryRequest request) {
        log.info("批量余票查询请求: date={}, from={}, to={}, trainNos={}",
                request.getTravelDate(), request.getFromStation(), request.getToStation(), request.getTrainNos());

        List<TicketQueryRequest> requests = request.getTrainNos().stream()
                .map(trainNo -> {
                    TicketQueryRequest req = new TicketQueryRequest();
                    req.setTrainNo(trainNo);
                    req.setTravelDate(request.getTravelDate());
                    req.setFromStation(request.getFromStation());
                    req.setToStation(request.getToStation());
                    return req;
                })
                .toList();

        List<TicketQueryVO> results = ticketQueryService.batchQueryTicket(requests);
        return Result.ok(results);
    }
}
