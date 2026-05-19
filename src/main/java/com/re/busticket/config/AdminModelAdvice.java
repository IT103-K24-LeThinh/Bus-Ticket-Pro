package com.re.busticket.config;

import com.re.busticket.entity.User;
import com.re.busticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

@ControllerAdvice(basePackages = "com.re.busticket.controller.admin")
@RequiredArgsConstructor
public class AdminModelAdvice {

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
}
