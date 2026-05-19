package com.re.busticket.service;

import com.re.busticket.dto.LocationFormDto;
import com.re.busticket.entity.Location;
import com.re.busticket.repository.LocationRepository;
import com.re.busticket.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final RouteRepository routeRepository;

    public Page<Location> findAll(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }

    public List<Location> findAll() {
        return locationRepository.findAll();
    }

    public Location findById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tỉnh thành với ID: " + id));
    }

    public Location create(LocationFormDto formDto) {
        if (existsByNameIgnoreCase(formDto.getName().trim())) {
            throw new IllegalArgumentException("Tên tỉnh thành đã tồn tại");
        }
        Location location = new Location();
        location.setName(formDto.getName().trim());
        return locationRepository.save(location);
    }

    public Location update(Long id, LocationFormDto formDto) {
        Location location = findById(id);
        if (existsByNameIgnoreCaseAndIdNot(formDto.getName().trim(), id)) {
            throw new IllegalArgumentException("Tên tỉnh thành đã tồn tại");
        }
        location.setName(formDto.getName().trim());
        return locationRepository.save(location);
    }

    public void delete(Long id) {
        findById(id);
        if (routeRepository.existsByDepartureLocationIdOrArrivalLocationId(id, id)) {
            throw new IllegalArgumentException("Không thể xóa tỉnh thành này vì đang được sử dụng trong tuyến đường");
        }
        locationRepository.deleteById(id);
    }

    public boolean existsByNameIgnoreCase(String name) {
        return locationRepository.existsByNameIgnoreCase(name);
    }

    public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id) {
        return locationRepository.existsByNameIgnoreCaseAndIdNot(name, id);
    }
}
