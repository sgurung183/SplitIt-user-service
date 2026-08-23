package com.SplitIt.user_service.repository;

import com.SplitIt.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    //these two methods will be used to ensure users with the email/phone dont already exist
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
}
