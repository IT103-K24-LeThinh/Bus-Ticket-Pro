package com.re.busticket.controller.admin;

import com.re.busticket.repository.LocationRepository;
import com.re.busticket.repository.RouteRepository;
import com.re.busticket.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminDashboardController {
    private final BusService busService;
    private final RouteRepository routeRepository;
    private final LocationRepository locationRepository;

    @GetMapping
    public String adminHome() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalBuses = busService.countAll();
        long activeBuses = busService.countActive();
        long inactiveBuses = Math.max(totalBuses - activeBuses, 0);
        long totalRoutes = routeRepository.count();
        long totalLocations = locationRepository.count();

        model.addAttribute("pageTitle", "Bảng điều khiển");
        model.addAttribute("currentPath", "/admin/dashboard");
        model.addAttribute("totalBuses", totalBuses);
        model.addAttribute("totalRoutes", totalRoutes);
        model.addAttribute("totalLocations", totalLocations);
        model.addAttribute("activeBuses", activeBuses);
        model.addAttribute("inactiveBuses", inactiveBuses);
        return "admin/dashboard";
    }
}
