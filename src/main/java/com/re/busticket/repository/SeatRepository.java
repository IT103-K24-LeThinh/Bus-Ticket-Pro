package com.re.busticket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.re.busticket.entity.Seat;
import com.re.busticket.entity.enums.SeatStatus;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByTripIdOrderBySeatNumberAsc(Long tripId);

    boolean existsByTripId(Long tripId);

    long countByTripIdAndStatusIn(Long tripId, List<SeatStatus> statuses);

    void deleteByTripId(Long tripId);

    List<Seat> findByTripIdAndLockedByUserId(Long tripId, Long lockedByUserId);
}
