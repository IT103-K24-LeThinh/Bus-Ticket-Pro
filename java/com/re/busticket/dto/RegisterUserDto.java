package com.re.busticket.dto;

import com.re.busticket.entity.enums.RoleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterUserDto {
    @NotBlank(message = "Username không được để trống")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username chỉ được chứa chữ cái và số, không có dấu cách")
    private String username;

    @NotBlank(message = "Full name không được để trống")
    private String fullName;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Address không được để trống")
    private String address;

    @NotBlank(message = "Phone không được để trống")
    @Pattern(regexp = "^\\d{11}$", message = "Phone phải có đúng 11 chữ số")
    private String phone;
}
