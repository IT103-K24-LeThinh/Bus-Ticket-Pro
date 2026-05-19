package com.re.busticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.re.busticket.entity.Route;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    boolean existsByDepartureLocationIdAndArrivalLocationId(Long departureId, Long arrivalId);

    boolean existsByDepartureLocationIdAndArrivalLocationIdAndIdNot(Long departureId, Long arrivalId, Long id);

    boolean existsByDepartureLocationIdOrArrivalLocationId(Long departureId, Long arrivalId);
}
