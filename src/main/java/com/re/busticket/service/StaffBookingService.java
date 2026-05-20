package com.re.busticket.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.Seat;
import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.BookingStatus;
import com.re.busticket.entity.enums.SeatStatus;
import com.re.busticket.repository.BookingRepository;
import com.re.busticket.repository.SeatRepository;
import com.re.busticket.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StaffBookingService {
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;

    public Page<Booking> findPendingBookings(Pageable pageable) {
        return bookingRepository.findByBookingStatusOrderByBookingTimeAsc(BookingStatus.PENDING, pageable);
    }

    public long countPending() {
        return bookingRepository.countByBookingStatus(BookingStatus.PENDING);
    }

    public long countOverdue() {
        List<Booking> pendingBookings = bookingRepository.findByBookingStatus(BookingStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();
        return pendingBookings.stream()
                .filter(b -> {
                    Trip trip = tripRepository.findById(b.getTripId()).orElse(null);
                    return trip != null && trip.getDepartureTime() != null && trip.getDepartureTime().isBefore(now);
                })
                .count();
    }

    public List<Booking> findOverdueBookings() {
        List<Booking> pendingBookings = bookingRepository.findByBookingStatus(BookingStatus.PENDING);
        LocalDateTime now = LocalDateTime.now();
        return pendingBookings.stream()
                .filter(b -> {
                    Trip trip = tripRepository.findById(b.getTripId()).orElse(null);
                    return trip != null && trip.getDepartureTime() != null && trip.getDepartureTime().isBefore(now);
                })
                .toList();
    }

    @Transactional
    public void confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé"));
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Vé không ở trạng thái chờ xác nhận");
        }
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        Seat seat = seatRepository.findById(booking.getSeatId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy ghế"));
        seat.setStatus(SeatStatus.BOOKED);
        bookingRepository.save(booking);
        seatRepository.save(seat);
    }

    @Transactional
    public void rejectBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé"));
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Vé không ở trạng thái có thể từ chối");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Seat seat = seatRepository.findById(booking.getSeatId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy ghế"));
        seat.setStatus(SeatStatus.AVAILABLE);
        bookingRepository.save(booking);
        seatRepository.save(seat);
    }

    @Transactional
    public void cancelOverdueBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vé"));
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Vé không thể huỷ");
        }
        Trip trip = tripRepository.findById(booking.getTripId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy chuyến"));
        if (trip.getDepartureTime() == null || !trip.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Vé chưa quá hạn");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
        Seat seat = seatRepository.findById(booking.getSeatId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy ghế"));
        seat.setStatus(SeatStatus.AVAILABLE);
        bookingRepository.save(booking);
        seatRepository.save(seat);
    }

    @Transactional
    public int bulkCancelOverdue() {
        List<Booking> overdueBookings = findOverdueBookings();
        for (Booking booking : overdueBookings) {
            booking.setBookingStatus(BookingStatus.CANCELLED);
            Seat seat = seatRepository.findById(booking.getSeatId()).orElse(null);
            if (seat != null) {
                seat.setStatus(SeatStatus.AVAILABLE);
                seatRepository.save(seat);
            }
            bookingRepository.save(booking);
        }
        return overdueBookings.size();
    }
}
