package com.re.busticket.controller.admin;

import com.re.busticket.entity.Bus;
import com.re.busticket.entity.Seat;
import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.SeatStatus;
import com.re.busticket.repository.BusRepository;
import com.re.busticket.service.SeatService;
import com.re.busticket.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/trips/{tripId}/seats")
public class AdminSeatController {

    private final SeatService seatService;
    private final TripService tripService;
    private final BusRepository busRepository;

    @GetMapping
    public String seats(@PathVariable Long tripId, Model model) {
        Trip trip = tripService.findById(tripId);
        Bus bus = busRepository.findById(trip.getBusId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe với ID: " + trip.getBusId()));

        if (!seatService.existsByTripId(tripId)) {
            seatService.generateSeatsForTrip(tripId, bus.getBusType());
        }

        List<Seat> seats = seatService.findByTripId(tripId);

        model.addAttribute("trip", trip);
        model.addAttribute("bus", bus);
        model.addAttribute("seats", seats);
        model.addAttribute("pageTitle", "Quản lý ghế");
        model.addAttribute("currentPath", "/admin/trips");
        model.addAttribute("backUrl", "/admin/trips");
        return "admin/trips/seats";
    }

    @PostMapping("/{seatId}/status")
    @ResponseBody
    public Map<String, Object> updateSeatStatus(@PathVariable Long tripId,
                                                 @PathVariable Long seatId,
                                                 @RequestParam("status") SeatStatus status) {
        Map<String, Object> response = new HashMap<>();
        try {
            seatService.updateStatus(seatId, status);
            response.put("success", true);
            response.put("message", "Cập nhật trạng thái ghế thành công");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
