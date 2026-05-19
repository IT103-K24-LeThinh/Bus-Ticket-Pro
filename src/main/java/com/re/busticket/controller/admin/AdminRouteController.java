package com.re.busticket.controller.admin;

import com.re.busticket.dto.RouteFormDto;
import com.re.busticket.entity.Location;
import com.re.busticket.entity.Route;
import com.re.busticket.service.LocationService;
import com.re.busticket.service.RouteService;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/routes")
public class AdminRouteController {
    private final RouteService routeService;
    private final LocationService locationService;

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size), Sort.by("id").descending());
        Page<Route> routes = routeService.findAll(pageable);

        Set<Long> locationIds = new HashSet<>();
        for (Route route : routes.getContent()) {
            if (route.getDepartureLocationId() != null) {
                locationIds.add(route.getDepartureLocationId());
            }
            if (route.getArrivalLocationId() != null) {
                locationIds.add(route.getArrivalLocationId());
            }
        }

        Map<Long, String> locationNameById = new HashMap<>();
        for (Location location : locationService.findAll()) {
            if (locationIds.contains(location.getId())) {
                locationNameById.put(location.getId(), location.getName());
            }
        }

        model.addAttribute("pageTitle", "Danh mục tuyến đường");
        model.addAttribute("currentPath", "/admin/routes");
        model.addAttribute("routes", routes);
        model.addAttribute("locationNameById", locationNameById);
        return "admin/routes/index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("routeForm")) {
            model.addAttribute("routeForm", new RouteFormDto());
        }
        configureFormModel(model, "Thêm tuyến đường mới");
        return "admin/routes/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("routeForm") RouteFormDto routeFormDto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            configureFormModel(model, "Thêm tuyến đường mới");
            return "admin/routes/form";
        }

        try {
            routeService.create(routeFormDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm tuyến đường thành công.");
        } catch (IllegalArgumentException ex) {
            configureFormModel(model, "Thêm tuyến đường mới");
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/routes/form";
        }
        return "redirect:/admin/routes";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("routeForm", routeService.findRouteFormById(id));
            configureFormModel(model, "Cập nhật tuyến đường");
            return "admin/routes/form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/routes";
        }
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("routeForm") RouteFormDto routeFormDto,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            configureFormModel(model, "Cập nhật tuyến đường");
            return "admin/routes/form";
        }

        try {
            routeService.update(id, routeFormDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật tuyến đường thành công.");
        } catch (IllegalArgumentException ex) {
            configureFormModel(model, "Cập nhật tuyến đường");
            model.addAttribute("errorMessage", ex.getMessage());
            return "admin/routes/form";
        }
        return "redirect:/admin/routes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            routeService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tuyến đường thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/routes";
    }

    private void configureFormModel(Model model, String pageTitle) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("currentPath", "/admin/routes");
        model.addAttribute("locations", locationService.findAll());
        model.addAttribute("backUrl", "/admin/routes");
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
