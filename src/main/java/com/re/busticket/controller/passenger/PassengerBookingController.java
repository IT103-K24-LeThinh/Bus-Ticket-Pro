package com.re.busticket.controller.passenger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.re.busticket.dto.BookingFormDto;
import com.re.busticket.entity.Booking;
import com.re.busticket.entity.Bus;
import com.re.busticket.entity.Route;
import com.re.busticket.entity.Seat;
import com.re.busticket.entity.Trip;
import com.re.busticket.entity.User;
import com.re.busticket.entity.enums.BookingStatus;
import com.re.busticket.entity.enums.PaymentMethod;
import com.re.busticket.repository.BusRepository;
import com.re.busticket.repository.LocationRepository;
import com.re.busticket.repository.RouteRepository;
import com.re.busticket.repository.UserRepository;
import com.re.busticket.service.BookingService;
import com.re.busticket.service.EmailService;
import com.re.busticket.entity.enums.SeatLockResult;
import com.re.busticket.service.SeatLockService;
import com.re.busticket.service.SeatService;
import com.re.busticket.service.TripService;
import com.re.busticket.service.exception.BookingNotFoundException;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/passenger/booking")
@RequiredArgsConstructor
public class PassengerBookingController {
    private final EmailService emailService;
    private final BookingService bookingService;
    private final TripService tripService;
    private final SeatService seatService;
    private final SeatLockService seatLockService;
    private final BusRepository busRepository;
    private final RouteRepository routeRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    @GetMapping("/confirm")
    public String confirm(@RequestParam Long tripId,
                          @RequestParam Long seatId,
                          Authentication authentication,
                          Model model,
                          RedirectAttributes ra) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy người dùng hiện tại"));

        SeatLockResult lockResult = seatLockService.tryLockSeat(seatId, tripId, currentUser.getId());
//        System.out.println(lockResult);

        switch (lockResult) {
            case LOCKED:
            case ALREADY_LOCKED_BY_SELF:
                break;
            case LOCKED_BY_OTHER:
                ra.addFlashAttribute("errorMessage", "Ghế đang được người khác giữ");
                return "redirect:/passenger/trips/" + tripId + "/seats";
            case SEAT_BOOKED:
                ra.addFlashAttribute("errorMessage", "Ghế không còn khả dụng");
                return "redirect:/passenger/trips/" + tripId + "/seats";
            case INVALID:
                ra.addFlashAttribute("errorMessage", "Lựa chọn không hợp lệ");
                return "redirect:/passenger/trips/" + tripId + "/seats";
        }

        Seat seat = seatService.findById(seatId);
        Trip trip = tripService.findById(tripId);

        Bus bus = busRepository.findById(trip.getBusId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy xe của chuyến với ID: " + trip.getBusId()));

        Route route = routeRepository.findById(trip.getRouteId())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy tuyến của chuyến với ID: " + trip.getRouteId()));

        String departureLocationName = locationRepository.findById(route.getDepartureLocationId())
                .map(loc -> loc.getName())
                .orElse("N/A");
        String arrivalLocationName = locationRepository.findById(route.getArrivalLocationId())
                .map(loc -> loc.getName())
                .orElse("N/A");

        BookingFormDto bookingForm = new BookingFormDto();
        bookingForm.setTripId(tripId);
        bookingForm.setSeatId(seatId);
        bookingForm.setPaymentMethod(PaymentMethod.CASH);
        bookingForm.setPassengerFullName(currentUser.getFullName());
        bookingForm.setPassengerPhone(currentUser.getPhone());
        bookingForm.setPassengerEmail(currentUser.getEmail());

        model.addAttribute("trip", trip);
        model.addAttribute("bus", bus);
        model.addAttribute("seat", seat);
        model.addAttribute("departureLocationName", departureLocationName);
        model.addAttribute("arrivalLocationName", arrivalLocationName);
        model.addAttribute("bookingForm", bookingForm);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("pageTitle", "Xác nhận đặt vé");
        model.addAttribute("currentPath", "/passenger/booking/confirm");
        model.addAttribute("backUrl", "/passenger/trips/" + tripId + "/seats");

        return "passenger/booking/confirm";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute BookingFormDto bookingForm,
                         Authentication authentication,
                         RedirectAttributes ra) {
        System.out.println(bookingForm.toString());
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy người dùng hiện tại"));

        try {
            Booking booking = bookingService.createBooking(bookingForm, currentUser.getId());
            if (bookingForm.getPaymentMethod() == PaymentMethod.CASH) {
                sendBookingEmail(booking, currentUser);
                ra.addFlashAttribute("successMessage", "Đặt vé thành công");
                return "redirect:/passenger/booking/" + booking.getId();
            }
            return "redirect:/passenger/payment/bank-transfer/" + booking.getId();
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/passenger/trips/" + bookingForm.getTripId() + "/seats";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes ra) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng"));

        Booking booking;
        try {
            booking = bookingService.findOwnedById(id, currentUser.getId());
        } catch (BookingNotFoundException ex) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy vé");
            return "redirect:/passenger/booking/history";
        }

        Trip trip = tripService.findById(booking.getTripId());
        Bus bus = busRepository.findById(trip.getBusId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy xe"));
        Route route = routeRepository.findById(trip.getRouteId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy tuyến"));
        String departureLocationName = locationRepository.findById(route.getDepartureLocationId())
                .map(loc -> loc.getName()).orElse("N/A");
        String arrivalLocationName = locationRepository.findById(route.getArrivalLocationId())
                .map(loc -> loc.getName()).orElse("N/A");
        Seat seat = seatService.findById(booking.getSeatId());

        boolean cancellable = bookingService.isCancellable(booking, trip);

        model.addAttribute("booking", booking);
        model.addAttribute("trip", trip);
        model.addAttribute("bus", bus);
        model.addAttribute("seat", seat);
        model.addAttribute("departureLocationName", departureLocationName);
        model.addAttribute("arrivalLocationName", arrivalLocationName);
        model.addAttribute("bookingReference", bookingService.toReference(booking));
        model.addAttribute("cancellable", cancellable);
        model.addAttribute("pageTitle", "Chi tiết vé");
        model.addAttribute("currentPath", "/passenger/booking/" + id);
        model.addAttribute("backUrl", "/passenger/booking/history");
        return "passenger/booking/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         Authentication authentication,
                         RedirectAttributes ra) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng"));

        try {
            bookingService.cancelBooking(id, currentUser.getId());
            ra.addFlashAttribute("successMessage", "Đã huỷ vé thành công.");
        } catch (org.springframework.security.access.AccessDeniedException ex) {
            ra.addFlashAttribute("errorMessage", "Bạn không có quyền huỷ vé này.");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/passenger/booking/history";
    }

    @GetMapping("/history")
    public String history(Authentication authentication,
                          @RequestParam(required = false) BookingStatus status,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size,
                          Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng"));

        int normalizedSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizedSize);

        Page<Booking> bookings = bookingService.findByUserId(currentUser.getId(), status, pageable);

        Set<Long> tripIds = new HashSet<>();
        for (Booking b : bookings.getContent()) {
            tripIds.add(b.getTripId());
        }

        Map<Long, String> tripRouteName = new HashMap<>();
        Map<Long, LocalDateTime> tripDepartureTime = new HashMap<>();
        Map<Long, String> seatNumberById = new HashMap<>();
        Map<Long, Boolean> cancellableMap = new HashMap<>();

        for (Long tripId : tripIds) {
            try {
                Trip trip = tripService.findById(tripId);
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
            } catch (Exception ex) {
                tripRouteName.put(tripId, "N/A");
            }
        }

        for (Booking b : bookings.getContent()) {
            try {
                Trip trip = tripService.findById(b.getTripId());
                cancellableMap.put(b.getId(), bookingService.isCancellable(b, trip));
                seatNumberById.put(b.getSeatId(), seatService.findById(b.getSeatId()).getSeatNumber());
            } catch (Exception ex) {
                cancellableMap.put(b.getId(), false);
            }
        }

        Map<Long, String> bookingReferenceMap = new HashMap<>();
        for (Booking b : bookings.getContent()) {
            bookingReferenceMap.put(b.getId(), bookingService.toReference(b));
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("tripRouteName", tripRouteName);
        model.addAttribute("tripDepartureTime", tripDepartureTime);
        model.addAttribute("seatNumberById", seatNumberById);
        model.addAttribute("cancellableMap", cancellableMap);
        model.addAttribute("bookingReferenceMap", bookingReferenceMap);
        model.addAttribute("filterStatus", status);
        model.addAttribute("pageTitle", "Lịch sử đặt vé");
        model.addAttribute("currentPath", "/passenger/booking/history");
        model.addAttribute("backUrl", "/passenger/dashboard");
        return "passenger/booking/history";
    }

    @GetMapping("/lookup")
    public String lookupForm(Model model) {
        model.addAttribute("pageTitle", "Tra cứu vé");
        model.addAttribute("currentPath", "/passenger/booking/lookup");
        model.addAttribute("backUrl", "/passenger/dashboard");
        return "passenger/booking/lookup";
    }

    @PostMapping("/lookup")
    public String lookupSubmit(@RequestParam(required = false) String bookingReference,
                                @RequestParam(required = false) String phoneNumber,
                                Authentication authentication,
                                Model model) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng"));

        model.addAttribute("pageTitle", "Tra cứu vé");
        model.addAttribute("currentPath", "/passenger/booking/lookup");
        model.addAttribute("backUrl", "/passenger/dashboard");
        model.addAttribute("bookingReference", bookingReference);
        model.addAttribute("phoneNumber", phoneNumber);

        if (bookingReference == null || bookingReference.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Vui lòng nhập mã vé");
            return "passenger/booking/lookup";
        }else if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Vui lòng nhập SĐT");
            return  "passenger/booking/lookup";
        }

        return bookingService.lookupByReference(bookingReference, phoneNumber)
                .map(booking -> "redirect:/passenger/booking/" + booking.getId())
                .orElseGet(() -> {
                    model.addAttribute("errorMessage", "Không tìm thấy vé với mã đã nhập");
                    return "passenger/booking/lookup";
                });
    }

    private void sendBookingEmail(Booking booking, User user) {
        try {
            Trip trip = tripService.findById(booking.getTripId());
            Bus bus = busRepository.findById(trip.getBusId()).orElse(null);
            Route route = routeRepository.findById(trip.getRouteId()).orElse(null);
            Seat seat = seatService.findById(booking.getSeatId());

            String routeName = "N/A";
            if (route != null) {
                String dep = locationRepository.findById(route.getDepartureLocationId())
                        .map(loc -> loc.getName()).orElse("N/A");
                String arr = locationRepository.findById(route.getArrivalLocationId())
                        .map(loc -> loc.getName()).orElse("N/A");
                routeName = dep + " → " + arr;
            }

            String paymentMethodLabel = booking.getPaymentMethod() == PaymentMethod.CASH
                    ? "Tiền mặt" : "Chuyển khoản";

            emailService.sendBookingConfirmationEmail(
                    user.getEmail(),
                    user.getFullName(),
                    bookingService.toReference(booking),
                    routeName,
                    trip.getDepartureTime(),
                    trip.getArrivalTime(),
                    seat.getSeatNumber(),
                    bus != null ? bus.getLicensePlate() : "N/A",
                    booking.getPaymentAmount(),
                    paymentMethodLabel
            );
        } catch (Exception e) {
        }
    }
}
