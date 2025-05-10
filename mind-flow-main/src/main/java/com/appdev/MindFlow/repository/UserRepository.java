package com.appdev.MindFlow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.appdev.MindFlow.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByActualUsername(String actualUsername);
}
