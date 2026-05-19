package com.re.busticket.repository;

import com.re.busticket.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    long countByIsActiveTrue();
}
