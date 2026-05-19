package com.re.busticket.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.re.busticket.entity.Seat;
import com.re.busticket.entity.enums.BusType;
import com.re.busticket.entity.enums.SeatStatus;
import com.re.busticket.repository.SeatRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;

    public List<Seat> findByTripId(Long tripId) {
        return seatRepository.findByTripIdOrderBySeatNumberAsc(tripId);
    }

    public Seat findById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ghế với ID: " + id));
    }

    public void updateStatus(Long seatId, SeatStatus status) {
        Seat seat = findById(seatId);
        seat.setStatus(status);
        seatRepository.save(seat);
    }

    @Transactional
    public void generateSeatsForTrip(Long tripId, BusType busType) {
        int totalSeats = switch (busType) {
            case SEATS_29 -> 29;
            case SEATS_45 -> 45;
        };

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= totalSeats; i++) {
            Seat seat = new Seat();
            seat.setTripId(tripId);
            seat.setSeatNumber(String.format("%02d", i));
            seat.setStatus(SeatStatus.AVAILABLE);
            seats.add(seat);
        }
        seatRepository.saveAll(seats);
    }

    @Transactional
    public void deleteByTripId(Long tripId) {
        seatRepository.deleteByTripId(tripId);
    }

    public boolean existsByTripId(Long tripId) {
        return seatRepository.existsByTripId(tripId);
    }

    public long countByTripIdAndStatusIn(Long tripId, List<SeatStatus> statuses) {
        return seatRepository.countByTripIdAndStatusIn(tripId, statuses);
    }
}
