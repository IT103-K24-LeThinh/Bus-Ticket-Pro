package com.re.busticket.controller.admin;

import com.re.busticket.dto.LocationFormDto;
import com.re.busticket.entity.Location;
import com.re.busticket.service.LocationService;
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
@RequestMapping("/admin/locations")
public class AdminLocationController {
    private final LocationService locationService;

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size), Sort.by("id").descending());
        Page<Location> locations = locationService.findAll(pageable);

        model.addAttribute("pageTitle", "Danh mục tỉnh thành");
        model.addAttribute("currentPath", "/admin/locations");
        model.addAttribute("locations", locations);
        return "admin/locations/index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("locationForm")) {
            model.addAttribute("locationForm", new LocationFormDto());
        }
        configureFormModel(model, "Thêm tỉnh thành mới");
        return "admin/locations/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("locationForm") LocationFormDto locationFormDto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            configureFormModel(model, "Thêm tỉnh thành mới");
            return "admin/locations/form";
        }

        try {
            locationService.create(locationFormDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm tỉnh thành thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/locations";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Location location = locationService.findById(id);
            model.addAttribute("locationForm", new LocationFormDto(location.getId(), location.getName()));
            configureFormModel(model, "Cập nhật tỉnh thành");
            return "admin/locations/form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/locations";
        }
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("locationForm") LocationFormDto locationFormDto,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            configureFormModel(model, "Cập nhật tỉnh thành");
            return "admin/locations/form";
        }

        try {
            locationService.update(id, locationFormDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tỉnh thành thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/locations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            locationService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tỉnh thành thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/locations";
    }

    private void configureFormModel(Model model, String pageTitle) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("currentPath", "/admin/locations");
        model.addAttribute("backUrl", "/admin/locations");
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
