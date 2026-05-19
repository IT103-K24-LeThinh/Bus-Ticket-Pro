package com.re.busticket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDto {
    private Long id;
    private String fullName;
    private String licenseNumber;
    private String phone;
}
