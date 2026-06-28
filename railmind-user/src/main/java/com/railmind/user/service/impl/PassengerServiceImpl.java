package com.railmind.user.service.impl;

import com.railmind.common.exception.BizException;
import com.railmind.common.exception.ErrorCode;
import com.railmind.common.util.CryptoUtil;
import com.railmind.user.domain.model.Passenger;
import com.railmind.user.dto.AddPassengerRequest;
import com.railmind.user.dto.PassengerDTO;
import com.railmind.user.dto.UpdatePassengerRequest;
import com.railmind.user.mapper.PassengerMapper;
import com.railmind.user.service.PassengerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PassengerServiceImpl implements PassengerService {

    private final PassengerMapper passengerMapper;

    private static final int MAX_PASSENGERS = 15;

    @Override
    public List<PassengerDTO> listPassengers(Long userId) {
        return passengerMapper.selectByUserIdAndDeleted(userId, 0).stream()
                .map(this::toPassengerDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PassengerDTO addPassenger(Long userId, AddPassengerRequest request) {
        log.info("Adding passenger: userId={}, name={}", userId, request.getName());

        int count = passengerMapper.countByUserIdAndDeleted(userId, 0);
        if (count >= MAX_PASSENGERS) {
            throw new BizException(ErrorCode.PASSENGER_LIMIT_EXCEEDED);
        }

        String idCardHash = CryptoUtil.sha256(request.getIdCard());
        String encryptedIdCard = CryptoUtil.aesEncrypt(request.getIdCard());

        LocalDateTime now = LocalDateTime.now();
        Passenger passenger = Passenger.builder()
                .userId(userId)
                .name(request.getName())
                .idCard(encryptedIdCard)
                .idCardHash(idCardHash)
                .phone(request.getPhone())
                .type(request.getType() != null ? request.getType() : 1)
                .createdAt(now)
                .updatedAt(now)
                .build();

        passengerMapper.insert(passenger);
        log.info("Passenger added: passengerId={}", passenger.getId());

        return toPassengerDTO(passenger);
    }

    @Override
    @Transactional
    public PassengerDTO updatePassenger(Long userId, Long passengerId, UpdatePassengerRequest request) {
        log.info("Updating passenger: userId={}, passengerId={}", userId, passengerId);

        Passenger passenger = passengerMapper.selectByIdAndUserIdAndDeleted(passengerId, userId, 0);
        if (passenger == null) {
            throw new BizException(ErrorCode.PASSENGER_NOT_FOUND);
        }

        if (request.getName() != null) {
            passenger.setName(request.getName());
        }
        if (request.getPhone() != null) {
            passenger.setPhone(request.getPhone());
        }
        if (request.getType() != null) {
            passenger.setType(request.getType());
        }

        passengerMapper.updateById(passenger);
        log.info("Passenger updated: passengerId={}", passengerId);

        return toPassengerDTO(passenger);
    }

    @Override
    @Transactional
    public void deletePassenger(Long userId, Long passengerId) {
        log.info("Deleting passenger: userId={}, passengerId={}", userId, passengerId);

        Passenger passenger = passengerMapper.selectByIdAndUserIdAndDeleted(passengerId, userId, 0);
        if (passenger == null) {
            throw new BizException(ErrorCode.PASSENGER_NOT_FOUND);
        }

        passengerMapper.deleteById(passengerId);

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
