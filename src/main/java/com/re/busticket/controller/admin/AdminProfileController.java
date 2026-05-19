package com.re.busticket.controller.admin;

import com.re.busticket.entity.User;
import com.re.busticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/profile")
public class AdminProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Hồ sơ cá nhân");
        model.addAttribute("currentPath", "/admin/profile");
        return "admin/profile";
    }

    @PostMapping
    public String updateProfile(Authentication authentication,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String address,
                                RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (fullName == null || fullName.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Họ tên không được để trống");
            return "redirect:/admin/profile";
        }

        if (email != null && !email.trim().isEmpty()
                && userRepository.existsByEmailAndIdNot(email.trim(), user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email đã được sử dụng bởi tài khoản khác");
            return "redirect:/admin/profile";
        }

        if (phone != null && !phone.trim().isEmpty()
                && userRepository.existsByPhoneAndIdNot(phone.trim(), user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại đã được sử dụng bởi tài khoản khác");
            return "redirect:/admin/profile";
        }

        user.setFullName(fullName.trim());
        user.setEmail(email != null ? email.trim() : user.getEmail());
        user.setPhone(phone != null ? phone.trim() : user.getPhone());
        user.setAddress(address != null ? address.trim() : user.getAddress());
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công");
        return "redirect:/admin/profile";
    }

    @PostMapping("/password")
    public String changePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu hiện tại không đúng");
            return "redirect:/admin/profile";
        }

        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới phải có ít nhất 6 ký tự");
            return "redirect:/admin/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu xác nhận không khớp");
            return "redirect:/admin/profile";
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công");
        return "redirect:/admin/profile";
    }
}
