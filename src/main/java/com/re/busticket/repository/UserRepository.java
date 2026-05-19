package com.re.busticket.repository;

import com.re.busticket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhone(String phone);
    boolean existsByPhoneAndIdNot(String phone, Long id);
    User findByPhone(String phone);

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    User findByEmail(String email);

    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
}
