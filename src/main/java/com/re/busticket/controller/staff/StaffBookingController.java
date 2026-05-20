package com.re.busticket.controller.staff;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.Route;
import com.re.busticket.entity.Trip;
import com.re.busticket.repository.LocationRepository;
import com.re.busticket.repository.RouteRepository;
import com.re.busticket.repository.SeatRepository;
import com.re.busticket.repository.TripRepository;
import com.re.busticket.repository.UserRepository;
import com.re.busticket.service.BookingService;
import com.re.busticket.service.StaffBookingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/staff/bookings")
@RequiredArgsConstructor
public class StaffBookingController {

    private final StaffBookingService staffBookingService;
    private final BookingService bookingService;
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final LocationRepository locationRepository;
    private final SeatRepository seatRepository;
    private final UserRepository userRepository;

    @GetMapping("/pending")
    public String pending(@RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        int normalizedSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizedSize);

        Page<Booking> bookings = staffBookingService.findPendingBookings(pageable);

        Map<Long, String> tripRouteName = new HashMap<>();
        Map<Long, LocalDateTime> tripDepartureTime = new HashMap<>();
        Map<Long, String> seatNumberById = new HashMap<>();
        Map<Long, String> userNameById = new HashMap<>();
        Map<Long, String> bookingReferenceMap = new HashMap<>();

        Set<Long> tripIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();

        for (Booking b : bookings.getContent()) {
            tripIds.add(b.getTripId());
            userIds.add(b.getUserId());
            bookingReferenceMap.put(b.getId(), bookingService.toReference(b));
        }

        for (Long tripId : tripIds) {
            Trip trip = tripRepository.findById(tripId).orElse(null);
            if (trip != null) {
                tripDepartureTime.put(tripId, trip.getDepartureTime());
                Route route = routeRepository.findById(trip.getRouteId()).orElse(null);
                if (route != null) {
                    String dep = locationRepository.findById(route.getDepartureLocationId())
                            .map(loc -> loc.getName()).orElse("N/A");
                    String arr = locationRepository.findById(route.getArrivalLocationId())
                            .map(loc -> loc.getName()).orElse("N/A");
                    tripRouteName.put(tripId, dep + " → " + arr);
                } else {
                    tripRouteName.put(tripId, "N/A");
                }
            } else {
                tripRouteName.put(tripId, "N/A");
            }
        }

        for (Long userId : userIds) {
            userRepository.findById(userId).ifPresent(user ->
                    userNameById.put(userId, user.getFullName()));
        }

        for (Booking b : bookings.getContent()) {
            seatRepository.findById(b.getSeatId()).ifPresent(seat ->
                    seatNumberById.put(b.getSeatId(), seat.getSeatNumber()));
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("tripRouteName", tripRouteName);
        model.addAttribute("tripDepartureTime", tripDepartureTime);
        model.addAttribute("seatNumberById", seatNumberById);
        model.addAttribute("userNameById", userNameById);
        model.addAttribute("bookingReferenceMap", bookingReferenceMap);
        model.addAttribute("pageTitle", "Chờ thanh toán");
        model.addAttribute("currentPath", "/staff/bookings/pending");
        model.addAttribute("backUrl", "/staff/dashboard");
        return "staff/bookings/pending";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, RedirectAttributes ra) {
        try {
            staffBookingService.confirmBooking(id);
            ra.addFlashAttribute("successMessage", "Đã xác nhận vé BK-" + id + " thành công");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/bookings/pending";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        try {
            staffBookingService.rejectBooking(id);
            ra.addFlashAttribute("successMessage", "Đã từ chối vé BK-" + id + " thành công");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/bookings/pending";
    }

    @GetMapping("/overdue")
    public String overdue(Model model) {
        List<Booking> overdueBookings = staffBookingService.findOverdueBookings();

        Map<Long, String> tripRouteName = new HashMap<>();
        Map<Long, LocalDateTime> tripDepartureTime = new HashMap<>();
        Map<Long, String> seatNumberById = new HashMap<>();
        Map<Long, String> userNameById = new HashMap<>();
        Map<Long, String> bookingReferenceMap = new HashMap<>();

        Set<Long> tripIds = new HashSet<>();
        Set<Long> userIds = new HashSet<>();

        for (Booking b : overdueBookings) {
            tripIds.add(b.getTripId());
            userIds.add(b.getUserId());
            bookingReferenceMap.put(b.getId(), bookingService.toReference(b));
        }

        for (Long tripId : tripIds) {
            Trip trip = tripRepository.findById(tripId).orElse(null);
            if (trip != null) {
                tripDepartureTime.put(tripId, trip.getDepartureTime());
                Route route = routeRepository.findById(trip.getRouteId()).orElse(null);
                if (route != null) {
                    String dep = locationRepository.findById(route.getDepartureLocationId())
                            .map(loc -> loc.getName()).orElse("N/A");
                    String arr = locationRepository.findById(route.getArrivalLocationId())
                            .map(loc -> loc.getName()).orElse("N/A");
                    tripRouteName.put(tripId, dep + " → " + arr);
                } else {
                    tripRouteName.put(tripId, "N/A");
                }
            } else {
                tripRouteName.put(tripId, "N/A");
            }
        }

        for (Long userId : userIds) {
            userRepository.findById(userId).ifPresent(user ->
                    userNameById.put(userId, user.getFullName()));
        }

        for (Booking b : overdueBookings) {
            seatRepository.findById(b.getSeatId()).ifPresent(seat ->
                    seatNumberById.put(b.getSeatId(), seat.getSeatNumber()));
        }

        model.addAttribute("overdueBookings", overdueBookings);
        model.addAttribute("tripRouteName", tripRouteName);
        model.addAttribute("tripDepartureTime", tripDepartureTime);
        model.addAttribute("seatNumberById", seatNumberById);
        model.addAttribute("userNameById", userNameById);
        model.addAttribute("bookingReferenceMap", bookingReferenceMap);
        model.addAttribute("pageTitle", "Vé quá hạn");
        model.addAttribute("currentPath", "/staff/bookings/overdue");
        model.addAttribute("backUrl", "/staff/dashboard");
        return "staff/bookings/overdue";
    }

    @PostMapping("/{id}/cancel-overdue")
    public String cancelOverdue(@PathVariable Long id, RedirectAttributes ra) {
        try {
            staffBookingService.cancelOverdueBooking(id);
            ra.addFlashAttribute("successMessage", "Đã huỷ vé BK-" + id + " thành công");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/bookings/overdue";
    }

    @PostMapping("/bulk-cancel-overdue")
    public String bulkCancelOverdue(RedirectAttributes ra) {
        int count = staffBookingService.bulkCancelOverdue();
        if (count == 0) {
            ra.addFlashAttribute("errorMessage", "Không có vé quá hạn nào để huỷ");
        } else {
            ra.addFlashAttribute("successMessage", "Đã huỷ " + count + " vé quá hạn thành công");
        }
        return "redirect:/staff/bookings/overdue";
    }
}
