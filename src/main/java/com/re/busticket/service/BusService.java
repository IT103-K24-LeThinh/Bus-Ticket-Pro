package com.re.busticket.service;

import com.re.busticket.dto.BusFormDto;
import com.re.busticket.entity.Bus;
import com.re.busticket.repository.BusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BusService {
    private final BusRepository busRepository;

    public Page<Bus> findAll(Pageable pageable) {
        return busRepository.findAll(pageable);
    }

    public List<Bus> findAll() {
        return busRepository.findAll();
    }

    public BusFormDto findBusFormById(Long id) {
        Bus bus = findById(id);
        BusFormDto formDto = new BusFormDto();
        formDto.setId(bus.getId());
        formDto.setLicensePlate(bus.getLicensePlate());
        formDto.setBusType(bus.getBusType());
        formDto.setTotalSeats(bus.getTotalSeats());
        formDto.setCompanyName(bus.getCompanyName());
        formDto.setDriverName(bus.getDriverName());
        formDto.setIsActive(bus.getIsActive());
        return formDto;
    }

    public Bus create(BusFormDto formDto) {
        Bus bus = new Bus();
        mapFormToEntity(formDto, bus);
        return busRepository.save(bus);
    }

    public Bus update(Long id, BusFormDto formDto) {
        Bus bus = findById(id);
        mapFormToEntity(formDto, bus);
        return busRepository.save(bus);
    }

    public void deactivate(Long id) {
        Bus bus = findById(id);
        bus.setIsActive(false);
        busRepository.save(bus);
    }

    public long countAll() {
        return busRepository.count();
    }

    public long countActive() {
        return busRepository.countByIsActiveTrue();
    }

    private Bus findById(Long id) {
        return busRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe với ID: " + id));
    }

    private void mapFormToEntity(BusFormDto formDto, Bus bus) {
        bus.setLicensePlate(formDto.getLicensePlate().trim());
        bus.setBusType(formDto.getBusType());
        bus.setTotalSeats(formDto.getTotalSeats());
        bus.setCompanyName(formDto.getCompanyName().trim());
        bus.setDriverName(formDto.getDriverName().trim());
        bus.setIsActive(Boolean.TRUE.equals(formDto.getIsActive()));
    }
}
