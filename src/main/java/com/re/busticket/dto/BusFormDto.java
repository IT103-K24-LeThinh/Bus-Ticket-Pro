package com.re.busticket.dto;

import com.re.busticket.entity.enums.BusType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusFormDto {
    private Long id;

    @NotBlank(message = "Biển số xe không được để trống")
    private String licensePlate;

    @NotNull(message = "Vui lòng chọn loại xe")
    private BusType busType;

    @NotNull(message = "Tổng số ghế không được để trống")
    @Min(value = 1, message = "Tổng số ghế phải lớn hơn 0")
    private Integer totalSeats;

    @NotBlank(message = "Tên hãng xe không được để trống")
    private String companyName;

    @NotBlank(message = "Vui lòng chọn tài xế")
    private String driverName;

    @NotNull(message = "Trạng thái hoạt động không hợp lệ")
    private Boolean isActive = true;
}
