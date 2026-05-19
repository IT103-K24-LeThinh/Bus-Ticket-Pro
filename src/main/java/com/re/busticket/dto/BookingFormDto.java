package com.re.busticket.dto;

import com.re.busticket.entity.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingFormDto {

    @NotNull(message = "Vui lòng chọn chuyến xe")
    private Long tripId;

    @NotNull(message = "Vui lòng chọn ghế")
    private Long seatId;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;

    private String passengerFullName;

    private String passengerPhone;

    private String passengerEmail;
}
