package com.re.busticket.controller.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.re.busticket.entity.User;
import com.re.busticket.entity.enums.RoleType;
import com.re.busticket.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    public String index(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50), Sort.by("id").descending());
        Page<User> users = userRepository.findAll(pageable);

        model.addAttribute("pageTitle", "Quản lý người dùng");
        model.addAttribute("currentPath", "/admin/users");
        model.addAttribute("users", users);
        return "admin/users/index";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes ra) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        User target = userRepository.findById(id).orElse(null);

        if (target == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy người dùng");
            return "redirect:/admin/users";
        }

        if (currentUser != null && currentUser.getId().equals(target.getId())) {
            ra.addFlashAttribute("errorMessage", "Không thể thay đổi trạng thái tài khoản của chính mình");
            return "redirect:/admin/users";
        }

        target.setActive(!target.isActive());
        userRepository.save(target);
        ra.addFlashAttribute("successMessage",
                target.isActive() ? "Đã kích hoạt tài khoản" : "Đã khóa tài khoản");
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/change-role")
    public String changeRole(@PathVariable Long id,
                             @RequestParam RoleType role,
                             Authentication authentication,
                             RedirectAttributes ra) {
        User currentUser = userRepository.findByUsername(authentication.getName()).orElse(null);
        User target = userRepository.findById(id).orElse(null);

        if (target == null) {
            ra.addFlashAttribute("errorMessage", "Không tìm thấy người dùng");
            return "redirect:/admin/users";
        }

        if (currentUser != null && currentUser.getId().equals(target.getId())) {
            ra.addFlashAttribute("errorMessage", "Không thể thay đổi role của chính mình");
            return "redirect:/admin/users";
        }

        if (role != RoleType.STAFF && role != RoleType.PASSENGER) {
            ra.addFlashAttribute("errorMessage", "Chỉ có thể chuyển giữa STAFF và PASSENGER");
            return "redirect:/admin/users";
        }

        target.setRole(role);
        userRepository.save(target);
        ra.addFlashAttribute("successMessage", "Đã đổi role thành " + role.name());
        return "redirect:/admin/users";
    }
}
