package com.uno.repository;

import com.uno.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    void deleteByUsername(String username);
    void deleteByEmail(String email);
    void deleteById(Long id);

    Optional<User> findById(Long id);

}
