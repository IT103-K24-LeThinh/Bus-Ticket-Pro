package com.re.busticket.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.re.busticket.dto.TripFormDto;
import com.re.busticket.entity.Bus;
import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.SeatStatus;
import com.re.busticket.repository.BusRepository;
import com.re.busticket.repository.SeatRepository;
import com.re.busticket.repository.TripRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripService {
    private final TripRepository tripRepository;
    private final SeatRepository seatRepository;
    private final BusRepository busRepository;
    private final SeatService seatService;

    public Page<Trip> findAll(Pageable pageable) {
        return tripRepository.findAll(pageable);
    }

    public Trip findById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chuyến xe với ID: " + id));
    }

    public TripFormDto findTripFormById(Long id) {
        Trip trip = findById(id);
        TripFormDto formDto = new TripFormDto();
        formDto.setId(trip.getId());
        formDto.setRouteId(trip.getRouteId());
        formDto.setBusId(trip.getBusId());
        formDto.setDepartureTime(trip.getDepartureTime());
        formDto.setArrivalTime(trip.getArrivalTime());
        formDto.setTicketPrice(trip.getTicketPrice());
        formDto.setStatus(trip.getStatus());
        return formDto;
    }

    @Transactional
    public Trip create(TripFormDto formDto) {
        validateArrivalAfterDeparture(formDto);
        Trip trip = new Trip();
        mapFormToEntity(formDto, trip);
        Trip saved = tripRepository.save(trip);

        Bus bus = busRepository.findById(formDto.getBusId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe với ID: " + formDto.getBusId()));
        seatService.generateSeatsForTrip(saved.getId(), bus.getBusType());

        return saved;
    }

    public Trip update(Long id, TripFormDto formDto) {
        validateArrivalAfterDeparture(formDto);
        Trip trip = findById(id);
        mapFormToEntity(formDto, trip);
        return tripRepository.save(trip);
    }

    @Transactional
    public void delete(Long id) {
        long bookedOrPendingCount = seatRepository.countByTripIdAndStatusIn(
                id, List.of(SeatStatus.BOOKED, SeatStatus.PENDING));
        if (bookedOrPendingCount > 0) {
            throw new IllegalArgumentException(
                    "Không thể xóa chuyến xe vì còn " + bookedOrPendingCount + " ghế đã đặt hoặc đang chờ");
        }
        seatRepository.deleteByTripId(id);
        tripRepository.deleteById(id);
    }

    public boolean hasBookedOrPendingSeats(Long tripId) {
        return seatRepository.countByTripIdAndStatusIn(
                tripId, List.of(SeatStatus.BOOKED, SeatStatus.PENDING)) > 0;
    }

    private void validateArrivalAfterDeparture(TripFormDto formDto) {
        if (formDto.getArrivalTime() != null && formDto.getDepartureTime() != null
                && !formDto.getArrivalTime().isAfter(formDto.getDepartureTime())) {
            throw new IllegalArgumentException("Thời gian đến phải sau thời gian khởi hành");
        }
    }

    private void mapFormToEntity(TripFormDto formDto, Trip trip) {
        trip.setRouteId(formDto.getRouteId());
        trip.setBusId(formDto.getBusId());
        trip.setDepartureTime(formDto.getDepartureTime());
        trip.setArrivalTime(formDto.getArrivalTime());
        trip.setTicketPrice(formDto.getTicketPrice());
        trip.setStatus(formDto.getStatus());
    }
}
