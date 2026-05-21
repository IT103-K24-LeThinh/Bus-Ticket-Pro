package com.re.busticket.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.enums.BookingStatus;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Page<Booking> findByUserIdOrderByBookingTimeDesc(Long userId, Pageable pageable);

    Page<Booking> findByUserIdAndBookingStatusOrderByBookingTimeDesc(Long userId, BookingStatus status, Pageable pageable);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    @Query("""
        SELECT b FROM Booking b
        JOIN User u ON u.id = b.userId
        WHERE b.id = :bookingId
        AND u.phone = :phoneNumber
    """)
    Optional<Booking> findByIdAndUserPhoneNumber(Long bookingId, String phoneNumber);

    Page<Booking> findByBookingStatusOrderByBookingTimeAsc(BookingStatus status, Pageable pageable);

    long countByBookingStatus(BookingStatus status);

    List<Booking> findByBookingStatus(BookingStatus status);

    List<Booking> findByBookingStatusAndBookingTimeBefore(BookingStatus status, LocalDateTime cutoff);

    List<Booking> findByTripIdAndBookingStatus(Long tripId, BookingStatus bookingStatus);

    @Query(value = "SELECT r.id, dep.name, arr.name, SUM(b.payment_amount) " +
            "FROM bookings b " +
            "JOIN trips t ON b.trip_id = t.id " +
            "JOIN routes r ON t.route_id = r.id " +
            "JOIN locations dep ON r.departure_location_id = dep.id " +
            "JOIN locations arr ON r.arrival_location_id = arr.id " +
            "WHERE b.status = 'CONFIRMED' " +
            "GROUP BY r.id, dep.name, arr.name " +
            "ORDER BY SUM(b.payment_amount) DESC", nativeQuery = true)
    List<Object[]> findRevenueByRoute();


    @Query(value = "SELECT YEAR(b.booking_time), MONTH(b.booking_time), SUM(b.payment_amount) " +
            "FROM bookings b " +
            "WHERE b.status = 'CONFIRMED' " +
            "GROUP BY YEAR(b.booking_time), MONTH(b.booking_time) " +
            "ORDER BY YEAR(b.booking_time) DESC, MONTH(b.booking_time) DESC", nativeQuery = true)
    List<Object[]> findRevenueByMonth();

    @Query(value = "SELECT t.id, dep.name, arr.name, t.departure_time, COUNT(b.id) " +
            "FROM bookings b " +
            "JOIN trips t ON b.trip_id = t.id " +
            "JOIN routes r ON t.route_id = r.id " +
            "JOIN locations dep ON r.departure_location_id = dep.id " +
            "JOIN locations arr ON r.arrival_location_id = arr.id " +
            "WHERE b.status = 'CONFIRMED' " +
            "GROUP BY t.id, dep.name, arr.name, t.departure_time " +
            "ORDER BY COUNT(b.id) DESC " +
            "LIMIT 5", nativeQuery = true)
    List<Object[]> findTop5TripsByBookingCount();
}
