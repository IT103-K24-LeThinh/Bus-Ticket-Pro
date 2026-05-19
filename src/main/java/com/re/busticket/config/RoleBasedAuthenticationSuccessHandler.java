package com.re.busticket.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String targetUrl = "/login";

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "ADMIN".equals(grantedAuthority.getAuthority()));
        boolean isPassenger = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> "PASSENGER".equals(grantedAuthority.getAuthority()));

        if (isAdmin) {
            targetUrl = "/admin/dashboard";
        } else if (isPassenger) {
            targetUrl = "/passenger/dashboard";
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
