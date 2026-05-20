package com.re.busticket.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

        model.addAttribute("pageTitle", "Bảng điều khiển");
        model.addAttribute("currentPath", "/admin/dashboard");
        model.addAttribute("totalBuses", totalBuses);
        model.addAttribute("totalRoutes", totalRoutes);
        model.addAttribute("totalTrips", totalTrips);
        model.addAttribute("activeBuses", activeBuses);
        model.addAttribute("inactiveBuses", inactiveBuses);
        return "admin/dashboard";
    }
}
