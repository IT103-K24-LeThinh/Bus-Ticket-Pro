package com.re.busticket.service;

import com.re.busticket.dto.DriverDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class DriverService {

    private static final List<DriverDto> MOCK_DRIVERS = List.of(
            new DriverDto(1L, "Nguyễn Văn An", "FC-001-2020", "0912345001"),
            new DriverDto(2L, "Trần Văn Bình", "FC-002-2021", "0912345002"),
            new DriverDto(3L, "Lê Văn Cường", "FC-003-2019", "0912345003"),
            new DriverDto(4L, "Phạm Quốc Dũng", "FC-004-2022", "0912345004"),
            new DriverDto(5L, "Đỗ Minh Đức", "FC-005-2020", "0912345005"),
            new DriverDto(6L, "Ngô Anh Huy", "FC-006-2021", "0912345006"),
            new DriverDto(7L, "Bùi Hoàng Long", "FC-007-2018", "0912345007"),
            new DriverDto(8L, "Vũ Gia Nam", "FC-008-2023", "0912345008"),
            new DriverDto(9L, "Phan Khánh Toàn", "FC-009-2022", "0912345009"),
            new DriverDto(10L, "Hoàng Trung Hiếu", "FC-010-2019", "0912345010")
    );

    public List<DriverDto> findAll() {
        return MOCK_DRIVERS;
    }

    public Page<DriverDto> findAll(Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), MOCK_DRIVERS.size());
        if (start >= MOCK_DRIVERS.size()) {
            return new PageImpl<>(List.of(), pageable, MOCK_DRIVERS.size());
        }
        return new PageImpl<>(MOCK_DRIVERS.subList(start, end), pageable, MOCK_DRIVERS.size());
    }

    public boolean existsByName(String driverName) {
        if (driverName == null || driverName.isBlank()) {
            return false;
        }
        String normalizedDriverName = driverName.trim().toLowerCase(Locale.ROOT);
        return MOCK_DRIVERS.stream()
                .anyMatch(driver -> driver.getFullName().trim().toLowerCase(Locale.ROOT).equals(normalizedDriverName));
    }
}
