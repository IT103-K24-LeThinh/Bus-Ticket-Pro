package com.re.busticket.controller.passenger;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/passenger")
public class PassengerDashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("message", "Chào mừng bạn đến trang hành khách.");
        return "passenger/dashboard";
    }
}
