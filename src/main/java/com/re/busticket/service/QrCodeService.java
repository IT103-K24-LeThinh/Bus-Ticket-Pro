package com.re.busticket.service;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

@Service
public class QrCodeService {

    private static final String SEPAY_QR_BASE = "https://qr.sepay.vn/img";
    private static final String ACCOUNT_NUMBER = "0943941773";
    private static final String BANK_CODE = "MB";
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;

    public String generateQrUrl(Long bookingId, double amount) {
        long wholeAmount = (long) amount;
        String description = formatTransferContent(bookingId);
        return SEPAY_QR_BASE
                + "?acc=" + ACCOUNT_NUMBER
                + "&bank=" + BANK_CODE
                + "&amount=" + wholeAmount
                + "&des=" + description;
    }

    public String formatTransferContent(Long bookingId) {
        return "CK " + bookingId;
    }

    public String formatAmountDisplay(double amount) {
        long wholeAmount = (long) amount;
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(wholeAmount) + " VND";
    }

    public long calculateRemainingSeconds(LocalDateTime bookingTime) {
        LocalDateTime expiryTime = bookingTime.plusMinutes(PAYMENT_TIMEOUT_MINUTES);
        LocalDateTime now = LocalDateTime.now();
        long remaining = Duration.between(now, expiryTime).getSeconds();
        return Math.max(0, remaining);
    }
}
