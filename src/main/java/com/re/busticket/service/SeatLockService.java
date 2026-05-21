package com.re.busticket.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.re.busticket.entity.enums.SeatLockResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.re.busticket.entity.Seat;
import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.SeatStatus;
import com.re.busticket.entity.enums.TripStatus;
import com.re.busticket.repository.SeatRepository;
import com.re.busticket.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatLockService {

    private static final int LOCK_DURATION_MINUTES = 10;

    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;

    @Transactional
    public SeatLockResult tryLockSeat(Long seatId, Long tripId, Long userId) {
        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (tripOpt.isEmpty() || tripOpt.get().getStatus() != TripStatus.SCHEDULED) {
            return SeatLockResult.INVALID;
        }

        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        if (seatOpt.isEmpty() || !tripId.equals(seatOpt.get().getTripId())) {
            return SeatLockResult.INVALID;
        }

        Seat seat = seatOpt.get();
        LocalDateTime now = LocalDateTime.now();

        if (seat.getStatus() == SeatStatus.BOOKED) {
            return SeatLockResult.SEAT_BOOKED;
        }

        if (seat.getStatus() == SeatStatus.PENDING && seat.getLockedUntil() != null && seat.getLockedUntil().isAfter(now)) {
            if (userId.equals(seat.getLockedByUserId())) {
                return SeatLockResult.ALREADY_LOCKED_BY_SELF;
            } else {
                return SeatLockResult.LOCKED_BY_OTHER;
            }
        }

        releaseUserLocksForTrip(tripId, userId, seatId);

        seat.setStatus(SeatStatus.PENDING);
        seat.setLockedByUserId(userId);
        seat.setLockedUntil(now.plusMinutes(LOCK_DURATION_MINUTES));
        seatRepository.save(seat);

        return SeatLockResult.LOCKED;
    }

    @Transactional
    public void clearLock(Long seatId) {
        Optional<Seat> seatOpt = seatRepository.findById(seatId);
        if (seatOpt.isPresent()) {
            Seat seat = seatOpt.get();
            seat.setLockedByUserId(null);
            seat.setLockedUntil(null);
            seatRepository.save(seat);
        }
    }

    private void releaseUserLocksForTrip(Long tripId, Long userId, Long excludeSeatId) {
        List<Seat> lockedSeats = seatRepository.findByTripIdAndLockedByUserId(tripId, userId);
        for (Seat lockedSeat : lockedSeats) {
            if (!lockedSeat.getId().equals(excludeSeatId)) {
                lockedSeat.setStatus(SeatStatus.AVAILABLE);
                lockedSeat.setLockedByUserId(null);
                lockedSeat.setLockedUntil(null);
                seatRepository.save(lockedSeat);
            }
        }
    }
}
