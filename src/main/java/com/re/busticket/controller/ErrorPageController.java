package com.re.busticket.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorPageController implements ErrorController {

    @GetMapping("/forbidden")
    public String forbiddenPage() {
        return "error/403";
    }

    @RequestMapping("/error")
    public String fallbackErrorPage(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode != null) {
            int status = Integer.parseInt(statusCode.toString());
            if (status == HttpStatus.FORBIDDEN.value()) {
                return "error/403";
            }
            model.addAttribute("statusCode", status);
        } else {
            model.addAttribute("statusCode", 500);
        }
        return "error/error";
    }
}
