package com.re.busticket.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteFormDto {
    private Long id;

    @NotNull(message = "Vui lòng chọn điểm đi")
    private Long departureLocationId;

    @NotNull(message = "Vui lòng chọn điểm đến")
    private Long arrivalLocationId;

    @NotNull(message = "Khoảng cách không được để trống")
    @Min(value = 1, message = "Khoảng cách phải từ 1 km")
    @Max(value = 9999, message = "Khoảng cách không được vượt quá 9999 km")
    private Double distanceKm;

    @NotNull(message = "Thời gian ước tính không được để trống")
    @Min(value = 1, message = "Thời gian ước tính phải từ 1 phút")
    @Max(value = 99999, message = "Thời gian ước tính không được vượt quá 99999 phút")
    private Integer estimatedDurationMinutes;
}
