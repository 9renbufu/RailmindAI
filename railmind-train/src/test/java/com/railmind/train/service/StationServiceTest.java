package com.railmind.train.service;

import com.railmind.common.exception.BizException;
import com.railmind.train.domain.model.Station;
import com.railmind.train.mapper.StationMapper;
import com.railmind.train.service.impl.StationServiceImpl;
import com.railmind.train.vo.StationVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private StationMapper stationMapper;

    @InjectMocks
    private StationServiceImpl stationService;

    private Station testStation;

    @BeforeEach
    void setUp() {
        testStation = Station.builder()
                .id(1L)
                .code("BJN")
                .name("北京南站")
                .city("北京")
                .province("北京")
                .longitude(new BigDecimal("116.378968"))
                .latitude(new BigDecimal("39.865225"))
                .bureau("北京局")
                .status(1)
                .build();
    }

    @Test
    void searchStations_shouldReturnResults() {
        when(stationMapper.searchByKeyword("北京")).thenReturn(Arrays.asList(testStation));

        List<StationVO> results = stationService.searchStations("北京");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("BJN", results.get(0).getCode());
        assertEquals("北京南站", results.get(0).getName());
        verify(stationMapper).searchByKeyword("北京");
    }

    @Test
    void getStationByCode_shouldReturnStation() {
        when(stationMapper.selectByCode("BJN")).thenReturn(testStation);

        StationVO result = stationService.getStationByCode("BJN");

        assertNotNull(result);
        assertEquals("BJN", result.getCode());
        assertEquals("北京南站", result.getName());
        assertEquals("北京", result.getCity());
    }

    @Test
    void getStationByCode_shouldThrowWhenNotFound() {
        when(stationMapper.selectByCode("INVALID")).thenReturn(null);

        assertThrows(BizException.class, () -> stationService.getStationByCode("INVALID"));
    }

    @Test
    void getStationsByCity_shouldReturnResults() {
        when(stationMapper.selectByCity("北京")).thenReturn(Arrays.asList(testStation));

        List<StationVO> results = stationService.getStationsByCity("北京");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("北京南站", results.get(0).getName());
    }
}
