package com.re.busticket.dto;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.re.busticket.entity.enums.TripStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripFormDto {
    private Long id;

    @NotNull(message = "Vui lòng chọn tuyến đường")
    private Long routeId;

    @NotNull(message = "Vui lòng chọn xe")
    private Long busId;

    @NotNull(message = "Thời gian khởi hành không được để trống")
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime departureTime;

    @NotNull(message = "Thời gian đến không được để trống")
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime arrivalTime;

    @NotNull(message = "Giá vé không được để trống")
    @Min(value = 1000, message = "Giá vé phải từ 1,000 VND")
    @Max(value = 99999999, message = "Giá vé không được vượt quá 99,999,999 VND")
    private Double ticketPrice;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private TripStatus status;
}
