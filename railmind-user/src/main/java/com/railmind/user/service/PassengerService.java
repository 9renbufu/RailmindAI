package com.railmind.user.service;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.common.util.CryptoUtil;
import com.railmind.user.domain.model.Passenger;
import com.railmind.user.domain.repository.PassengerRepository;
import com.railmind.user.dto.AddPassengerRequest;
import com.railmind.user.dto.PassengerDTO;
import com.railmind.user.dto.UpdatePassengerRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;

    private static final int MAX_PASSENGERS = 15;

    public List<PassengerDTO> listPassengers(Long userId) {
        return passengerRepository.findByUserIdAndDeleted(userId, 0).stream()
                .map(this::toPassengerDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PassengerDTO addPassenger(Long userId, AddPassengerRequest request) {
        log.info("Adding passenger: userId={}, name={}", userId, request.getName());

        long count = passengerRepository.countByUserIdAndDeleted(userId, 0);
        if (count >= MAX_PASSENGERS) {
            throw new BizException(ErrorCode.PASSENGER_LIMIT_EXCEEDED);
        }

        String idCardHash = CryptoUtil.sha256(request.getIdCard());
        String encryptedIdCard = CryptoUtil.aesEncrypt(request.getIdCard());

        Passenger passenger = Passenger.builder()
                .userId(userId)
                .name(request.getName())
                .idCard(encryptedIdCard)
                .idCardHash(idCardHash)
                .phone(request.getPhone())
                .type(request.getType() != null ? request.getType() : 1)
                .build();

        passenger = passengerRepository.save(passenger);
        log.info("Passenger added: passengerId={}", passenger.getId());

        return toPassengerDTO(passenger);
    }

    @Transactional
    public PassengerDTO updatePassenger(Long userId, Long passengerId, UpdatePassengerRequest request) {
        log.info("Updating passenger: userId={}, passengerId={}", userId, passengerId);

        Passenger passenger = passengerRepository.findByIdAndUserIdAndDeleted(passengerId, userId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.PASSENGER_NOT_FOUND));

        if (request.getName() != null) {
            passenger.setName(request.getName());
        }
        if (request.getPhone() != null) {
            passenger.setPhone(request.getPhone());
        }
        if (request.getType() != null) {
            passenger.setType(request.getType());
        }

        passenger = passengerRepository.save(passenger);
        log.info("Passenger updated: passengerId={}", passengerId);

        return toPassengerDTO(passenger);
    }

    @Transactional
    public void deletePassenger(Long userId, Long passengerId) {
        log.info("Deleting passenger: userId={}, passengerId={}", userId, passengerId);

        Passenger passenger = passengerRepository.findByIdAndUserIdAndDeleted(passengerId, userId, 0)
                .orElseThrow(() -> new BizException(ErrorCode.PASSENGER_NOT_FOUND));

        passenger.setDeleted(1);
        passengerRepository.save(passenger);

        log.info("Passenger deleted: passengerId={}", passengerId);
    }

    private PassengerDTO toPassengerDTO(Passenger passenger) {
        return PassengerDTO.builder()
                .id(passenger.getId())
                .userId(passenger.getUserId())
                .name(passenger.getName())
                .maskedIdCard(CryptoUtil.maskIdCard(CryptoUtil.aesDecrypt(passenger.getIdCard())))
                .phone(passenger.getPhone())
                .type(passenger.getType())
                .typeName(getPassengerTypeName(passenger.getType()))
                .status(passenger.getStatus())
                .createdAt(passenger.getCreatedAt())
                .build();
    }

    private String getPassengerTypeName(Integer type) {
        return switch (type) {
            case 1 -> "成人";
            case 2 -> "儿童";
            case 3 -> "学生";
            case 4 -> "军人";
            default -> "未知";
        };
    }
}
