package com.railmind.user.service;

import com.railmind.common.exception.BizException;
import com.railmind.common.util.CryptoUtil;
import com.railmind.user.domain.model.Passenger;
import com.railmind.user.dto.AddPassengerRequest;
import com.railmind.user.dto.PassengerDTO;
import com.railmind.user.mapper.PassengerMapper;
import com.railmind.user.service.impl.PassengerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PassengerServiceTest {

    @Mock
    private PassengerMapper passengerMapper;

    @InjectMocks
    private PassengerServiceImpl passengerService;

    private Passenger testPassenger;

    @BeforeEach
    void setUp() {
        testPassenger = Passenger.builder()
                .id(1L)
                .userId(1L)
                .name("张三")
                .idCard(CryptoUtil.aesEncrypt("110101199001011234"))
                .idCardHash(CryptoUtil.sha256("110101199001011234"))
                .phone("13800001111")
                .type(1)
                .status(1)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void listPassengers_shouldReturnList() {
        when(passengerMapper.selectByUserIdAndDeleted(1L, 0))
                .thenReturn(List.of(testPassenger));

        List<PassengerDTO> result = passengerService.listPassengers(1L);

        assertEquals(1, result.size());
        assertEquals("张三", result.get(0).getName());
    }

    @Test
    void addPassenger_shouldSucceed() {
        AddPassengerRequest request = new AddPassengerRequest();
        request.setName("李四");
        request.setIdCard("110101199901011234");
        request.setPhone("13800002222");
        request.setType(1);

        when(passengerMapper.countByUserIdAndDeleted(1L, 0)).thenReturn(0);
        when(passengerMapper.insert(any(Passenger.class))).thenReturn(1);

        PassengerDTO result = passengerService.addPassenger(1L, request);

        assertNotNull(result);
        assertEquals("李四", result.getName());
        verify(passengerMapper).insert(any(Passenger.class));
    }

    @Test
    void addPassenger_shouldFailWhenLimitExceeded() {
        AddPassengerRequest request = new AddPassengerRequest();
        request.setName("李四");
        request.setIdCard("110101199901011234");

        when(passengerMapper.countByUserIdAndDeleted(1L, 0)).thenReturn(15);

        assertThrows(BizException.class, () -> passengerService.addPassenger(1L, request));
    }

    @Test
    void deletePassenger_shouldSucceed() {
        when(passengerMapper.selectByIdAndUserIdAndDeleted(1L, 1L, 0))
                .thenReturn(testPassenger);
        when(passengerMapper.deleteById(1L)).thenReturn(1);

        passengerService.deletePassenger(1L, 1L);

        verify(passengerMapper).deleteById(1L);
    }

    @Test
    void deletePassenger_shouldFailWhenNotFound() {
        when(passengerMapper.selectByIdAndUserIdAndDeleted(99L, 1L, 0))
                .thenReturn(null);

        assertThrows(BizException.class, () -> passengerService.deletePassenger(1L, 99L));
    }
}
