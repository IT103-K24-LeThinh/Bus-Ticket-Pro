package com.re.busticket.controller.admin;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.re.busticket.repository.BookingRepository;
import com.re.busticket.repository.LocationRepository;
import com.re.busticket.repository.RouteRepository;
import com.re.busticket.repository.TripRepository;
import com.re.busticket.service.BusService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {
    private final BusService busService;
    private final RouteRepository routeRepository;
    private final LocationRepository locationRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalBuses = busService.countAll();
        long totalRoutes = routeRepository.count();
        long totalTrips = tripRepository.count();
        long activeBuses = busService.countActive();
        long inactiveBuses = Math.max(totalBuses - activeBuses, 0);

        List<Object[]> revenueByRoute = bookingRepository.findRevenueByRoute();

        List<Object[]> revenueByMonth = bookingRepository.findRevenueByMonth();

        List<Object[]> top5Trips = bookingRepository.findTop5TripsByBookingCount();

        model.addAttribute("pageTitle", "Bảng điều khiển");
        model.addAttribute("currentPath", "/admin/dashboard");
        model.addAttribute("totalBuses", totalBuses);
        model.addAttribute("totalRoutes", totalRoutes);
        model.addAttribute("totalTrips", totalTrips);
        model.addAttribute("activeBuses", activeBuses);
        model.addAttribute("inactiveBuses", inactiveBuses);
        model.addAttribute("revenueByRoute", revenueByRoute);
        model.addAttribute("revenueByMonth", revenueByMonth);
        model.addAttribute("top5Trips", top5Trips);
        return "admin/dashboard";
    }
}
