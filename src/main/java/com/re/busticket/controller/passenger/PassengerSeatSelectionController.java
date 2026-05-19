package com.re.busticket.controller.passenger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.re.busticket.entity.Bus;
import com.re.busticket.entity.Location;
import com.re.busticket.entity.Route;
import com.re.busticket.entity.Seat;
import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.TripStatus;
import com.re.busticket.repository.BusRepository;
import com.re.busticket.repository.LocationRepository;
import com.re.busticket.repository.RouteRepository;
import com.re.busticket.service.SeatService;
import com.re.busticket.service.TripService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/passenger/trips")
public class PassengerSeatSelectionController {

    private final TripService tripService;
    private final SeatService seatService;
    private final BusRepository busRepository;
    private final LocationRepository locationRepository;
    private final RouteRepository routeRepository;

    @GetMapping("/{tripId}/seats")
    public String seats(@PathVariable Long tripId,
                        @RequestParam(required = false) Long departureLocationId,
                        @RequestParam(required = false) Long arrivalLocationId,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        String searchUrl = buildSearchUrl(departureLocationId, arrivalLocationId, travelDate);

        Trip trip;
        try {
            trip = tripService.findById(tripId);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:" + searchUrl;
        }

        LocalDateTime now = LocalDateTime.now();
        if (trip.getStatus() != TripStatus.SCHEDULED
                || trip.getDepartureTime() == null
                || !trip.getDepartureTime().isAfter(now)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Chuyến xe không còn khả dụng để đặt vé.");
            return "redirect:" + searchUrl;
        }

        Bus bus = busRepository.findById(trip.getBusId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy xe của chuyến với ID: " + trip.getBusId()));

        Route route = routeRepository.findById(trip.getRouteId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy tuyến của chuyến với ID: " + trip.getRouteId()));

        String departureLocationName = locationRepository.findById(route.getDepartureLocationId())
                .map(Location::getName)
                .orElse("N/A");
        String arrivalLocationName = locationRepository.findById(route.getArrivalLocationId())
                .map(Location::getName)
                .orElse("N/A");

        List<Seat> seats = seatService.findByTripId(tripId);

        model.addAttribute("trip", trip);
        model.addAttribute("bus", bus);
        model.addAttribute("seats", seats);
        model.addAttribute("departureLocationName", departureLocationName);
        model.addAttribute("arrivalLocationName", arrivalLocationName);
        model.addAttribute("departureLocationId", departureLocationId);
        model.addAttribute("arrivalLocationId", arrivalLocationId);
        model.addAttribute("travelDate", travelDate);
        model.addAttribute("currentPath", "/passenger/trips/" + tripId + "/seats");
        model.addAttribute("pageTitle", "Chọn ghế");
        model.addAttribute("backUrl", searchUrl);

        return "passenger/trips/seats";
    }

    private String buildSearchUrl(Long departureLocationId, Long arrivalLocationId, LocalDate travelDate) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/passenger/trips/search");
        if (departureLocationId != null) {
            builder.queryParam("departureLocationId", departureLocationId);
        }
        if (arrivalLocationId != null) {
            builder.queryParam("arrivalLocationId", arrivalLocationId);
        }
        if (travelDate != null) {
            builder.queryParam("travelDate", travelDate);
        }
        return builder.build().toUriString();
    }
}
