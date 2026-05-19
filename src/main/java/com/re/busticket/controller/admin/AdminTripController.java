package com.re.busticket.controller.admin;

import com.re.busticket.dto.TripFormDto;
import com.re.busticket.entity.Bus;
import com.re.busticket.entity.Location;
import com.re.busticket.entity.Route;
import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.TripStatus;
import com.re.busticket.service.BusService;
import com.re.busticket.service.LocationService;
import com.re.busticket.service.RouteService;
import com.re.busticket.service.TripService;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/trips")
public class AdminTripController {
    private final TripService tripService;
    private final RouteService routeService;
    private final BusService busService;
    private final LocationService locationService;

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size), Sort.by("id").descending());
        Page<Trip> trips = tripService.findAll(pageable);

        Map<Long, String> locationNameById = new HashMap<>();
        for (Location location : locationService.findAll()) {
            locationNameById.put(location.getId(), location.getName());
        }

        Set<Long> routeIds = new HashSet<>();
        for (Trip trip : trips.getContent()) {
            if (trip.getRouteId() != null) {
                routeIds.add(trip.getRouteId());
            }
        }

        Map<Long, String> routeNameById = new HashMap<>();
        for (Long routeId : routeIds) {
            try {
                Route route = routeService.findById(routeId);
                String departure = locationNameById.getOrDefault(route.getDepartureLocationId(), "N/A");
                String arrival = locationNameById.getOrDefault(route.getArrivalLocationId(), "N/A");
                routeNameById.put(routeId, departure + " → " + arrival);
            } catch (IllegalArgumentException e) {
                routeNameById.put(routeId, "N/A");
            }
        }

        Map<Long, String> busNameById = new HashMap<>();
        for (Bus bus : busService.findAll()) {
            busNameById.put(bus.getId(), bus.getLicensePlate());
        }

        model.addAttribute("pageTitle", "Quản lý chuyến xe");
        model.addAttribute("currentPath", "/admin/trips");
        model.addAttribute("trips", trips);
        model.addAttribute("routeNameById", routeNameById);
        model.addAttribute("busNameById", busNameById);
        return "admin/trips/index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        if (!model.containsAttribute("tripForm")) {
            model.addAttribute("tripForm", new TripFormDto());
        }
        configureFormModel(model, "Thêm chuyến xe mới");
        return "admin/trips/form";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("tripForm") TripFormDto tripFormDto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            configureFormModel(model, "Thêm chuyến xe mới");
            return "admin/trips/form";
        }

        try {
            tripService.create(tripFormDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm chuyến xe thành công.");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            configureFormModel(model, "Thêm chuyến xe mới");
            return "admin/trips/form";
        }
        return "redirect:/admin/trips";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("tripForm", tripService.findTripFormById(id));
            configureFormModel(model, "Cập nhật chuyến xe");
            return "admin/trips/form";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/trips";
        }
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("tripForm") TripFormDto tripFormDto,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            configureFormModel(model, "Cập nhật chuyến xe");
            return "admin/trips/form";
        }

        try {
            tripService.update(id, tripFormDto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật chuyến xe thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/trips";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tripService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa chuyến xe thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/trips";
    }

    private void configureFormModel(Model model, String pageTitle) {
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("currentPath", "/admin/trips");
        model.addAttribute("backUrl", "/admin/trips");
        model.addAttribute("tripStatuses", TripStatus.values());
        model.addAttribute("routes", buildRouteDisplayList());
        model.addAttribute("buses", busService.findAll());
    }

    private List<Map<String, Object>> buildRouteDisplayList() {
        Map<Long, String> locationNameById = new HashMap<>();
        for (Location location : locationService.findAll()) {
            locationNameById.put(location.getId(), location.getName());
        }

        List<Route> routes = routeService.findAll();
        List<Map<String, Object>> routeDisplayList = new java.util.ArrayList<>();
        for (Route route : routes) {
            Map<String, Object> routeMap = new HashMap<>();
            routeMap.put("id", route.getId());
            String departure = locationNameById.getOrDefault(route.getDepartureLocationId(), "N/A");
            String arrival = locationNameById.getOrDefault(route.getArrivalLocationId(), "N/A");
            routeMap.put("name", departure + " → " + arrival);
            routeDisplayList.add(routeMap);
        }
        return routeDisplayList;
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 50);
    }
}
