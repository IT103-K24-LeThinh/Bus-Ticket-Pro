package com.re.busticket.controller.passenger;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.User;
import com.re.busticket.repository.UserRepository;
import com.re.busticket.service.BookingService;
import com.re.busticket.service.QrCodeService;
import com.re.busticket.service.exception.BookingNotFoundException;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/passenger/payment")
@RequiredArgsConstructor
public class PassengerPaymentController {

    private final BookingService bookingService;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;

    @Value("${sepay.account.number}")
    private String bankAccountNumber;

    @Value("${sepay.bank.code}")
    private String bankName;

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
        double paymentAmount = booking.getPaymentAmount();

        String qrCodeUrl = qrCodeService.generateQrUrl(bookingId, paymentAmount);
        String transferContent = qrCodeService.formatTransferContent(bookingId);
        String formattedAmount = qrCodeService.formatAmountDisplay(paymentAmount);
        long remainingSeconds = qrCodeService.calculateRemainingSeconds(booking.getBookingTime());

        model.addAttribute("booking", booking);
        model.addAttribute("bookingReference", bookingReference);
        model.addAttribute("paymentAmount", paymentAmount);
        model.addAttribute("qrCodeUrl", qrCodeUrl);
        model.addAttribute("transferContent", transferContent);
        model.addAttribute("formattedAmount", formattedAmount);
        model.addAttribute("remainingSeconds", remainingSeconds);
        model.addAttribute("bankAccountNumber", bankAccountNumber);
        model.addAttribute("bankName", bankName);
        model.addAttribute("pageTitle", "Thanh toán chuyển khoản");
        model.addAttribute("currentPath", "/passenger/payment/bank-transfer");
        model.addAttribute("backUrl", "/passenger/booking/history");

        return "passenger/payment/bank-transfer";
    }

    @GetMapping("/status/{bookingId}")
    @ResponseBody
    public ResponseEntity<Map<String, String>> getPaymentStatus(
            @PathVariable Long bookingId,
            Authentication authentication) {
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("status", "UNAUTHORIZED"));
        }

        try {
            Booking booking = bookingService.findOwnedById(bookingId, currentUser.getId());
            return ResponseEntity.ok(Map.of("status", booking.getBookingStatus().name()));
        } catch (BookingNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of("status", "NOT_FOUND"));
        }
    }
}
