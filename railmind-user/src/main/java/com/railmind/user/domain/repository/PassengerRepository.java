package com.railmind.user.domain.repository;

import com.railmind.user.domain.model.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

    List<Passenger> findByUserIdAndDeleted(Long userId, Integer deleted);

    Optional<Passenger> findByIdAndUserIdAndDeleted(Long id, Long userId, Integer deleted);

    long countByUserIdAndDeleted(Long userId, Integer deleted);
}
