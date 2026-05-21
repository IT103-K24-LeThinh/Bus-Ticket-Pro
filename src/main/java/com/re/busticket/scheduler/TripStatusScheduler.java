package com.re.busticket.scheduler;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.Seat;
import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.BookingStatus;
import com.re.busticket.entity.enums.SeatStatus;
import com.re.busticket.entity.enums.TripStatus;
import com.re.busticket.repository.BookingRepository;
import com.re.busticket.repository.SeatRepository;
import com.re.busticket.repository.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TripStatusScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TripStatusScheduler.class);

    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final TransactionTemplate transactionTemplate;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public TripStatusScheduler(TripRepository tripRepository,
                               BookingRepository bookingRepository,
                               SeatRepository seatRepository,
                               TransactionTemplate transactionTemplate) {
        this.tripRepository = tripRepository;
        this.bookingRepository = bookingRepository;
        this.seatRepository = seatRepository;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(cron = "0 0/5 * * * *")
    public void updateTripStatuses() {
        if (!running.compareAndSet(false, true)) {
            logger.info("TripStatusScheduler: Previous execution still running, skipping this cycle.");
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();

            List<Trip> scheduledTrips = tripRepository.findByStatusAndDepartureTimeBefore(
                    TripStatus.SCHEDULED, now);

            for (Trip trip : scheduledTrips) {
                processScheduledToDeparted(trip);
            }

            List<Trip> departedTrips = tripRepository.findByStatusAndArrivalTimeBefore(
                    TripStatus.DEPARTED, now);

            for (Trip trip : departedTrips) {
                processDepartedToCompleted(trip);
            }
        } finally {
            running.set(false);
        }
    }

    private void processScheduledToDeparted(Trip trip) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                trip.setStatus(TripStatus.DEPARTED);
                tripRepository.save(trip);

                List<Booking> pendingBookings = bookingRepository.findByTripIdAndBookingStatus(
                        trip.getId(), BookingStatus.PENDING);

                for (Booking booking : pendingBookings) {
                    booking.setBookingStatus(BookingStatus.CANCELLED);
                    bookingRepository.save(booking);

                    Optional<Seat> seatOpt = seatRepository.findById(booking.getSeatId());
                    seatOpt.ifPresent(seat -> {
                        seat.setStatus(SeatStatus.AVAILABLE);
                        seat.setLockedByUserId(null);
                        seat.setLockedUntil(null);
                        seatRepository.save(seat);
                    });
                }
            });
            logger.info("TripStatusScheduler: Trip {} transitioned from SCHEDULED to DEPARTED. Cancelled {} pending bookings.",
                    trip.getId(), bookingRepository.findByTripIdAndBookingStatus(trip.getId(), BookingStatus.CANCELLED).size());
        } catch (Exception e) {
            logger.error("TripStatusScheduler: Failed to process trip {} (SCHEDULED→DEPARTED): {}",
                    trip.getId(), e.getMessage(), e);
        }
    }

    private void processDepartedToCompleted(Trip trip) {
        try {
            transactionTemplate.executeWithoutResult(status -> {
                trip.setStatus(TripStatus.COMPLETED);
                tripRepository.save(trip);
            });
            logger.info("TripStatusScheduler: Trip {} transitioned from DEPARTED to COMPLETED.", trip.getId());
        } catch (Exception e) {
            logger.error("TripStatusScheduler: Failed to process trip {} (DEPARTED→COMPLETED): {}",
                    trip.getId(), e.getMessage(), e);
        }
    }
}
