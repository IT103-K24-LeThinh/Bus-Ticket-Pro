package com.re.busticket.repository;

import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    boolean existsByRouteId(Long routeId);

    List<Trip> findByRouteIdAndDepartureTimeBetweenAndStatusOrderByDepartureTimeAsc(
            Long routeId, LocalDateTime from, LocalDateTime to, TripStatus status);
}
