package com.railmind.user.service;

import com.railmind.user.dto.AddPassengerRequest;
import com.railmind.user.dto.PassengerDTO;
import com.railmind.user.dto.UpdatePassengerRequest;

import java.util.List;

public interface PassengerService {

    List<PassengerDTO> listPassengers(Long userId);

    PassengerDTO addPassenger(Long userId, AddPassengerRequest request);

    PassengerDTO updatePassenger(Long userId, Long passengerId, UpdatePassengerRequest request);

    void deletePassenger(Long userId, Long passengerId);
}
