package com.re.busticket.dto;

import com.re.busticket.entity.enums.SeatStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusUpdateDto {
    @NotNull(message = "Trạng thái không hợp lệ")
    private SeatStatus status;
}
