package com.re.busticket.repository;

import com.re.busticket.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    boolean existsByRouteId(Long routeId);
}
