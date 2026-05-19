package com.re.busticket.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TripSearchDto {

    @NotNull(message = "Vui lòng chọn điểm đi")
    private Long departureLocationId;

    @NotNull(message = "Vui lòng chọn điểm đến")
    private Long arrivalLocationId;

    @NotNull(message = "Vui lòng chọn ngày khởi hành")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate travelDate;
}
