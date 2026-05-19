package com.re.busticket.repository;

import com.re.busticket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPhone(String phone);
    User findByPhone(String phone);

    boolean existsByEmail(String email);
    User findByEmail(String email);

    boolean existsByUsername(String username);
    User findByUsername(String username);
}
