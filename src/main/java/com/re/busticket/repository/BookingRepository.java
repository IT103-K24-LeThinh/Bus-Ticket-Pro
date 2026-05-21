package com.re.busticket.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserIdOrderByBookingTimeDesc(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndBookingStatusOrderByBookingTimeDesc(Long userId, BookingStatus status, Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    Page<Booking> findByBookingStatusOrderByBookingTimeAsc(BookingStatus status, Pageable pageable);

    long countByBookingStatus(BookingStatus status);

    List<Booking> findByBookingStatus(BookingStatus status);

    List<Booking> findByBookingStatusAndBookingTimeBefore(BookingStatus status, LocalDateTime cutoff);

    List<Booking> findByTripIdAndBookingStatus(Long tripId, BookingStatus bookingStatus);
}
