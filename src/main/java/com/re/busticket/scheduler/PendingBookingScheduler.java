package com.re.busticket.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.enums.BookingStatus;
import com.re.busticket.repository.BookingRepository;

@Component
public class PendingBookingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PendingBookingScheduler.class);
    private static final int EXPIRY_MINUTES = 30;

    private final BookingRepository bookingRepository;
    private final PendingBookingCanceller pendingBookingCanceller;

    public PendingBookingScheduler(BookingRepository bookingRepository,
                                   PendingBookingCanceller pendingBookingCanceller) {
        this.bookingRepository = bookingRepository;
        this.pendingBookingCanceller = pendingBookingCanceller;
    }

    @Scheduled(fixedRate = 600000)
    public void cancelExpiredBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(EXPIRY_MINUTES);
        List<Booking> expiredBookings = bookingRepository.findByBookingStatusAndBookingTimeBefore(
                BookingStatus.PENDING, cutoff);

        logger.info("Found {} expired pending bookings to cancel", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            try {
                pendingBookingCanceller.cancelBooking(booking.getId());
            } catch (Exception e) {
                logger.error("Failed to cancel expired booking id={}: {}", booking.getId(), e.getMessage(), e);
            }
        }
    }
}
