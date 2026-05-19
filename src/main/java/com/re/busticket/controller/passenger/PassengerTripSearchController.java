package com.re.busticket.controller.passenger;

import com.re.busticket.dto.TripSearchResult;
import com.re.busticket.repository.LocationRepository;
import com.re.busticket.service.TripSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/passenger/trips")
public class PassengerTripSearchController {

    private final TripSearchService tripSearchService;
    private final LocationRepository locationRepository;

    @GetMapping("/search")
    public String search(@RequestParam(required = false) Long departureLocationId,
                         @RequestParam(required = false) Long arrivalLocationId,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate,
                         Model model) {
        model.addAttribute("locations", locationRepository.findAllByOrderByNameAsc());
        model.addAttribute("departureLocationId", departureLocationId);
        model.addAttribute("arrivalLocationId", arrivalLocationId);
        model.addAttribute("travelDate", travelDate);

        boolean searched = departureLocationId != null && arrivalLocationId != null;

        if (searched) {
            String errorMessage = null;
            if (departureLocationId.equals(arrivalLocationId)) {
                errorMessage = "Điểm đi và điểm đến phải khác nhau";
            } else if (travelDate != null && travelDate.isBefore(LocalDate.now())) {
                errorMessage = "Ngày khởi hành phải từ hôm nay trở đi";
            }

            if (errorMessage != null) {
                model.addAttribute("errorMessage", errorMessage);
            } else {
                List<TripSearchResult> results = tripSearchService.search(
                        departureLocationId, arrivalLocationId, travelDate);
                model.addAttribute("results", results);
                model.addAttribute("searchPerformed", true);
            }
        }

        model.addAttribute("pageTitle", "Tra cứu chuyến xe");
        model.addAttribute("currentPath", "/passenger/trips/search");
        model.addAttribute("backUrl", "/passenger/dashboard");
        return "passenger/trips/search";
    }
}
