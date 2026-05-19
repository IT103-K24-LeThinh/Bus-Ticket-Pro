package com.re.busticket.service;

import com.re.busticket.dto.TripSearchResult;
import com.re.busticket.entity.Bus;
import com.re.busticket.entity.Location;
import com.re.busticket.entity.Route;
import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.SeatStatus;
import com.re.busticket.entity.enums.TripStatus;
import com.re.busticket.repository.BusRepository;
import com.re.busticket.repository.LocationRepository;
import com.re.busticket.repository.RouteRepository;
import com.re.busticket.repository.SeatRepository;
import com.re.busticket.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TripSearchService {

    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;
    private final BusRepository busRepository;
    private final LocationRepository locationRepository;
    private final SeatRepository seatRepository;

    public List<TripSearchResult> search(Long departureLocationId, Long arrivalLocationId, LocalDate travelDate) {
        Optional<Route> routeOpt = routeRepository
                .findByDepartureLocationIdAndArrivalLocationId(departureLocationId, arrivalLocationId);
        if (routeOpt.isEmpty()) {
            return Collections.emptyList();
        }
        Route route = routeOpt.get();

        LocalDateTime from;
        LocalDateTime to;
        if (travelDate != null) {
            from = travelDate.atStartOfDay();
            to = travelDate.plusDays(1).atStartOfDay();
        } else {
            from = LocalDateTime.now();
            to = LocalDateTime.now().plusYears(1);
        }

        List<Trip> trips = tripRepository
                .findByRouteIdAndDepartureTimeBetweenAndStatusOrderByDepartureTimeAsc(
                        route.getId(), from, to, TripStatus.SCHEDULED);

        if (trips.isEmpty()) {
            return Collections.emptyList();
        }

        // Load location names once to avoid N+1 queries.
        String departureLocationName = locationRepository.findById(departureLocationId)
                .map(Location::getName)
                .orElse(null);
        String arrivalLocationName = locationRepository.findById(arrivalLocationId)
                .map(Location::getName)
                .orElse(null);

        List<TripSearchResult> results = new ArrayList<>(trips.size());
        for (Trip trip : trips) {
            Bus bus = busRepository.findById(trip.getBusId()).orElse(null);
            if (bus == null) {
                continue;
            }

            long availableSeats = seatRepository
                    .countByTripIdAndStatusIn(trip.getId(), List.of(SeatStatus.AVAILABLE));

            String busTypeDisplay = bus.getBusType() != null ? bus.getBusType().getDisplayName() : null;

            results.add(new TripSearchResult(
                    trip.getId(),
                    departureLocationName,
                    arrivalLocationName,
                    trip.getDepartureTime(),
                    trip.getArrivalTime(),
                    trip.getTicketPrice(),
                    bus.getLicensePlate(),
                    busTypeDisplay,
                    availableSeats
            ));
        }
        return results;
    }
}
