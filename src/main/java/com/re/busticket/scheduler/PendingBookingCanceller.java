package com.re.busticket.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.Seat;
import com.re.busticket.entity.enums.BookingStatus;
import com.re.busticket.entity.enums.SeatStatus;
import com.re.busticket.repository.BookingRepository;
import com.re.busticket.repository.SeatRepository;

@Service
public class PendingBookingCanceller {

    private static final Logger logger = LoggerFactory.getLogger(PendingBookingCanceller.class);

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    public PendingBookingCanceller(BookingRepository bookingRepository, SeatRepository seatRepository) {
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public void cancelBooking(long bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            logger.warn("Booking id={} not found, skipping", bookingId);
            return;
        }

        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            logger.info("Booking id={} is no longer PENDING (status={}), skipping",
                    bookingId, booking.getBookingStatus());
            return;
        }

        booking.setBookingStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        Seat seat = seatRepository.findById(booking.getSeatId()).orElse(null);
        if (seat != null) {
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setLockedByUserId(null);
            seat.setLockedUntil(null);
            seatRepository.save(seat);
        }

        logger.info("Cancelled expired booking id={} and released seat id={}", bookingId, booking.getSeatId());
    }
}
