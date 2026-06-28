package com.railmind.train.service;

import com.railmind.common.exception.BizException;
import com.railmind.train.domain.model.Train;
import com.railmind.train.domain.model.TrainSchedule;
import com.railmind.train.dto.ScheduleCreateRequest;
import com.railmind.train.mapper.TrainMapper;
import com.railmind.train.mapper.TrainScheduleMapper;
import com.railmind.train.service.impl.ScheduleServiceImpl;
import com.railmind.train.vo.ScheduleVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private TrainMapper trainMapper;

    @Mock
    private TrainScheduleMapper trainScheduleMapper;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private Train testTrain;
    private TrainSchedule testSchedule;

    @BeforeEach
    void setUp() {
        testTrain = Train.builder()
                .id(1L)
                .trainNo("G1")
                .trainType("G")
                .startStation("BJP")
                .endStation("SHH")
                .departureTime(LocalTime.of(9, 0))
                .arrivalTime(LocalTime.of(13, 28))
                .totalMileage(1318)
                .status(1)
                .build();

        testSchedule = TrainSchedule.builder()
                .id(1L)
                .trainId(1L)
                .travelDate(LocalDate.of(2026, 7, 15))
                .status(1)
                .build();
    }

    @Test
    void getSchedule_shouldReturnSchedule() {
        when(trainMapper.selectByTrainNo("G1")).thenReturn(testTrain);
        when(trainScheduleMapper.selectByTrainIdAndDate(1L, LocalDate.of(2026, 7, 15))).thenReturn(testSchedule);

        ScheduleVO result = scheduleService.getSchedule("G1", LocalDate.of(2026, 7, 15));

        assertNotNull(result);
        assertEquals("G1", result.getTrainNo());
        assertEquals(LocalDate.of(2026, 7, 15), result.getTravelDate());
        assertEquals(1, result.getStatus());
        assertEquals("正常", result.getStatusName());
    }

    @Test
    void getSchedule_shouldThrowWhenTrainNotFound() {
        when(trainMapper.selectByTrainNo("INVALID")).thenReturn(null);

        assertThrows(BizException.class, () -> scheduleService.getSchedule("INVALID", LocalDate.of(2026, 7, 15)));
    }

    @Test
    void getSchedule_shouldThrowWhenScheduleNotFound() {
        when(trainMapper.selectByTrainNo("G1")).thenReturn(testTrain);
        when(trainScheduleMapper.selectByTrainIdAndDate(1L, LocalDate.of(2026, 7, 15))).thenReturn(null);

        assertThrows(BizException.class, () -> scheduleService.getSchedule("G1", LocalDate.of(2026, 7, 15)));
    }

    @Test
    void createSchedule_shouldCreateSuccessfully() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setTrainNo("G1");
        request.setTravelDate(LocalDate.of(2026, 7, 16));
        request.setStatus(1);

        when(trainMapper.selectByTrainNo("G1")).thenReturn(testTrain);
        when(trainScheduleMapper.selectByTrainIdAndDate(1L, LocalDate.of(2026, 7, 16))).thenReturn(null);
        when(trainScheduleMapper.insert(any(TrainSchedule.class))).thenReturn(1);

        ScheduleVO result = scheduleService.createSchedule(request);

        assertNotNull(result);
        assertEquals("G1", result.getTrainNo());
        assertEquals(LocalDate.of(2026, 7, 16), result.getTravelDate());
        verify(trainScheduleMapper).insert(any(TrainSchedule.class));
    }

    @Test
    void createSchedule_shouldThrowWhenAlreadyExists() {
        ScheduleCreateRequest request = new ScheduleCreateRequest();
        request.setTrainNo("G1");
        request.setTravelDate(LocalDate.of(2026, 7, 15));
        request.setStatus(1);

        when(trainMapper.selectByTrainNo("G1")).thenReturn(testTrain);
        when(trainScheduleMapper.selectByTrainIdAndDate(1L, LocalDate.of(2026, 7, 15))).thenReturn(testSchedule);

        assertThrows(BizException.class, () -> scheduleService.createSchedule(request));
    }
}
