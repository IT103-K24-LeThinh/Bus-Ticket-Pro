package com.re.busticket.config;

import com.re.busticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice(basePackages = "com.re.busticket.controller.passenger")
@RequiredArgsConstructor
public class PassengerModelAdvice {

    private final UserRepository userRepository;

    @ModelAttribute
    public void addCurrentUser(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String username = auth.getName();
            userRepository.findByUsername(username).ifPresent(user -> {
                model.addAttribute("currentUser", user);
            });
        }
    }

    @ModelAttribute("bookingStatusLabels")
    public Map<String, String> bookingStatusLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("PENDING", "Chờ xử lý");
        labels.put("CONFIRMED", "Đã xác nhận");
        labels.put("CANCELLED", "Đã huỷ");
        return labels;
    }

    @ModelAttribute("paymentMethodLabels")
    public Map<String, String> paymentMethodLabels() {
        Map<String, String> labels = new LinkedHashMap<>();
        labels.put("CASH", "Tiền mặt");
        labels.put("BANK_TRANSFER", "Chuyển khoản");
        return labels;
    }
}
