package com.railmind.user.domain.repository;

import com.railmind.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByUsername(String username);

    Optional<User> findByPhoneAndDeleted(String phone, Integer deleted);

    Optional<User> findByUsernameAndDeleted(String username, Integer deleted);

    boolean existsByPhone(String phone);

    boolean existsByUsername(String username);
}
