package com.re.busticket.controller.admin;

import com.re.busticket.dto.DriverDto;
import com.re.busticket.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/drivers")
public class AdminDriverController {
    private final DriverService driverService;

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size));
        Page<DriverDto> drivers = driverService.findAll(pageable);

        model.addAttribute("pageTitle", "Danh mục tài xế (Mock)");
        model.addAttribute("currentPath", "/admin/drivers");
        model.addAttribute("drivers", drivers);
        return "admin/drivers/index";
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
