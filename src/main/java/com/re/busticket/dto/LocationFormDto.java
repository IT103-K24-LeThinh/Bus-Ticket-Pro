package com.re.busticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationFormDto {
    private Long id;

    @NotBlank(message = "Tên tỉnh thành không được để trống")
    @Size(max = 100, message = "Tên tỉnh thành không được vượt quá 100 ký tự")
    private String name;
}
