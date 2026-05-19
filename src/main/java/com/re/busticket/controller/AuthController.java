package com.re.busticket.controller;

import com.re.busticket.dto.RegisterUserDto;
import com.re.busticket.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping()
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Đăng ký thành công, vui lòng đăng nhập.");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new RegisterUserDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("user") RegisterUserDto registerUserDto,
                           BindingResult bindingResult,
                           Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.registerUser(registerUserDto);
            return "redirect:/login?registered=1";
        } catch (RuntimeException ex) {
            bindingResult.reject("register.error", ex.getMessage());
            return "auth/register";
        }
    }
}
