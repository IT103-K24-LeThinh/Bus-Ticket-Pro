package com.re.busticket.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.TripStatus;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    boolean existsByRouteId(Long routeId);

    List<Trip> findByRouteIdAndDepartureTimeBetweenAndStatusOrderByDepartureTimeAsc(
            Long routeId, LocalDateTime from, LocalDateTime to, TripStatus status);

    List<Trip> findByStatusAndDepartureTimeBefore(TripStatus status, LocalDateTime time);

    List<Trip> findByStatusAndArrivalTimeBefore(TripStatus status, LocalDateTime time);
}
