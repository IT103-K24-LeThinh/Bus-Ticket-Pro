package com.re.busticket.controller.passenger;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.User;
import com.re.busticket.repository.UserRepository;
import com.re.busticket.service.BookingService;
import com.re.busticket.service.exception.BookingNotFoundException;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/passenger/payment")
@RequiredArgsConstructor
public class PassengerPaymentController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    @GetMapping("/bank-transfer/{bookingId}")
    public String bankTransfer(@PathVariable Long bookingId,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy người dùng hiện tại"));

        Booking booking;
        try {
            booking = bookingService.findOwnedById(bookingId, currentUser.getId());
        } catch (BookingNotFoundException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy vé");
            return "redirect:/passenger/booking/history";
        }

        String bookingReference = bookingService.toReference(booking);

        model.addAttribute("booking", booking);
        model.addAttribute("bookingReference", bookingReference);
        model.addAttribute("paymentAmount", booking.getPaymentAmount());
        model.addAttribute("pageTitle", "Thanh toán chuyển khoản");
        model.addAttribute("currentPath", "/passenger/payment/bank-transfer");
        model.addAttribute("backUrl", "/passenger/booking/history");

        return "passenger/payment/bank-transfer";
    }
}
