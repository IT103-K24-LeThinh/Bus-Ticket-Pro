package com.re.busticket.repository;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserIdOrderByBookingTimeDesc(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndBookingStatusOrderByBookingTimeDesc(Long userId, BookingStatus status, Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);
}
