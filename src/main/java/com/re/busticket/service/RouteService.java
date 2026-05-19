package com.re.busticket.service;

import com.re.busticket.dto.RouteFormDto;
import com.re.busticket.entity.Route;
import com.re.busticket.repository.RouteRepository;
import com.re.busticket.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RouteService {
    private final RouteRepository routeRepository;
    private final TripRepository tripRepository;

    public Page<Route> findAll(Pageable pageable) {
        return routeRepository.findAll(pageable);
    }

    public List<Route> findAll() {
        return routeRepository.findAll();
    }

    public Route findById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tuyến đường với ID: " + id));
    }

    public RouteFormDto findRouteFormById(Long id) {
        Route route = findById(id);
        RouteFormDto formDto = new RouteFormDto();
        formDto.setId(route.getId());
        formDto.setDepartureLocationId(route.getDepartureLocationId());
        formDto.setArrivalLocationId(route.getArrivalLocationId());
        formDto.setDistanceKm(route.getDistanceKm());
        formDto.setEstimatedDurationMinutes(route.getEstimatedDurationMinutes());
        return formDto;
    }

    public Route create(RouteFormDto formDto) {
        validateDepartureArrivalDifferent(formDto.getDepartureLocationId(), formDto.getArrivalLocationId());
        validateDuplicatePair(formDto.getDepartureLocationId(), formDto.getArrivalLocationId());

        Route route = new Route();
        mapFormToEntity(formDto, route);
        return routeRepository.save(route);
    }

    public Route update(Long id, RouteFormDto formDto) {
        Route route = findById(id);
        validateDepartureArrivalDifferent(formDto.getDepartureLocationId(), formDto.getArrivalLocationId());
        validateDuplicatePairExcluding(formDto.getDepartureLocationId(), formDto.getArrivalLocationId(), id);

        mapFormToEntity(formDto, route);
        return routeRepository.save(route);
    }

    public void delete(Long id) {
        if (tripRepository.existsByRouteId(id)) {
            throw new IllegalArgumentException("Không thể xóa tuyến đường này vì đang có chuyến xe liên kết");
        }
        routeRepository.deleteById(id);
    }

    private void validateDepartureArrivalDifferent(Long departureId, Long arrivalId) {
        if (departureId != null && departureId.equals(arrivalId)) {
            throw new IllegalArgumentException("Điểm đi và điểm đến không được trùng nhau");
        }
    }

    private void validateDuplicatePair(Long departureId, Long arrivalId) {
        if (routeRepository.existsByDepartureLocationIdAndArrivalLocationId(departureId, arrivalId)) {
            throw new IllegalArgumentException("Tuyến đường với cặp điểm đi - điểm đến này đã tồn tại");
        }
    }

    private void validateDuplicatePairExcluding(Long departureId, Long arrivalId, Long excludeId) {
        if (routeRepository.existsByDepartureLocationIdAndArrivalLocationIdAndIdNot(departureId, arrivalId, excludeId)) {
            throw new IllegalArgumentException("Tuyến đường với cặp điểm đi - điểm đến này đã tồn tại");
        }
    }

    private void mapFormToEntity(RouteFormDto formDto, Route route) {
        route.setDepartureLocationId(formDto.getDepartureLocationId());
        route.setArrivalLocationId(formDto.getArrivalLocationId());
        route.setDistanceKm(formDto.getDistanceKm());
        route.setEstimatedDurationMinutes(formDto.getEstimatedDurationMinutes());
    }
}
