package com.re.busticket.controller;

import com.re.busticket.dto.RegisterUserDto;
import com.re.busticket.entity.User;
import com.re.busticket.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class UserController {
    public final AuthService authService;

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") RegisterUserDto registerUserDto,
                           BindingResult bindingResult,
                           Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            User user = authService.registerUser(registerUserDto);
            model.addAttribute("message", "Đăng ký thành công!");
            return "hello";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }
}
