package com.re.busticket.controller;

import com.re.busticket.dto.ResponseSepayDto;
import com.re.busticket.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${sepay.api.token}")
    private String sepayWebhookToken;

    private final PaymentService paymentService;

    @PostMapping("/sepay")
    public ResponseEntity<Map<String, Boolean>> processPayment(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) ResponseSepayDto request
    ) {
        if (!paymentService.validateToken(authorization, sepayWebhookToken)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false));
        }

        System.err.println(request.toString());

        if (request == null || request.getId() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false));
        }

        Long bookingId = paymentService.parseBookingIdFromContent(request.getContent());
        System.err.println("bookingId: " + bookingId);
        if (bookingId == null) {
            return ResponseEntity.ok(Map.of("success", true));
        }

        paymentService.confirmPayment(bookingId, request.getTransferAmount());

        return ResponseEntity.ok(Map.of("success", true));
    }
}
