package com.re.busticket.controller.admin;

import com.re.busticket.dto.BusFormDto;
import com.re.busticket.entity.Bus;
import com.re.busticket.entity.enums.BusType;
import com.re.busticket.service.BusService;
import com.re.busticket.service.DriverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/buses")
public class AdminBusController {
    private final BusService busService;
    private final DriverService driverService;

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size), Sort.by("id").descending());
        Page<Bus> buses = busService.findAll(pageable);

        model.addAttribute("pageTitle", "Quản lý xe");
        model.addAttribute("currentPath", "/admin/buses");
        model.addAttribute("buses", buses);
        return "admin/buses/index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("busForm")) {
            model.addAttribute("busForm", new BusFormDto(null, "", null, null, "", "", true));
        }
        configureFormModel(model, "Thêm xe mới");
        return "admin/buses/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("busForm") BusFormDto busFormDto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        validateDriverSelection(busFormDto, bindingResult);
        if (bindingResult.hasErrors()) {
            configureFormModel(model, "Thêm xe mới");
            return "admin/buses/form";
        }

        busService.create(busFormDto);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm xe thành công.");
        return "redirect:/admin/buses";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("busForm", busService.findBusFormById(id));
            configureFormModel(model, "Cập nhật thông tin xe");
            return "admin/buses/form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/buses";
        }
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("busForm") BusFormDto busFormDto,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        validateDriverSelection(busFormDto, bindingResult);
        if (bindingResult.hasErrors()) {
            configureFormModel(model, "Cập nhật thông tin xe");
            return "admin/buses/form";
        }

        try {
            busService.update(id, busFormDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật xe thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/buses";
    }

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            busService.deactivate(id);
            redirectAttributes.addFlashAttribute("warningMessage", "Đã chuyển xe sang trạng thái ngừng hoạt động.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/buses";
    }

    private void configureFormModel(Model model, String pageTitle) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("currentPath", "/admin/buses");
        model.addAttribute("busTypes", BusType.values());
        model.addAttribute("drivers", driverService.findAll());
    }

    private void validateDriverSelection(BusFormDto busFormDto, BindingResult bindingResult) {
        if (!bindingResult.hasFieldErrors("driverName") && !driverService.existsByName(busFormDto.getDriverName())) {
            bindingResult.rejectValue("driverName", "driver.invalid", "Tài xế không hợp lệ.");
        }
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
