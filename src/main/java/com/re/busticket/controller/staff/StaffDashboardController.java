package com.re.busticket.controller.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.re.busticket.service.StaffBookingService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffDashboardController {

    private final StaffBookingService staffBookingService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("countPending", staffBookingService.countPending());
        model.addAttribute("countOverdue", staffBookingService.countOverdue());
        model.addAttribute("pageTitle", "Bảng điều khiển");
        model.addAttribute("currentPath", "/staff/dashboard");
        return "staff/dashboard";
    }
}
