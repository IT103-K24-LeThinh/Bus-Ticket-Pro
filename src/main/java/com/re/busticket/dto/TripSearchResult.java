package com.re.busticket.dto;

import java.time.LocalDateTime;

public record TripSearchResult(
        Long tripId,
        String departureLocationName,
        String arrivalLocationName,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        Double ticketPrice,
        String busLicensePlate,
        String busTypeDisplay,
        long availableSeats
) {
}
