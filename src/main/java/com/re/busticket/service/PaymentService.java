package com.re.busticket.service;

import com.re.busticket.entity.Booking;
import com.re.busticket.entity.Seat;
import com.re.busticket.entity.enums.BookingStatus;
import com.re.busticket.entity.enums.SeatStatus;
import com.re.busticket.repository.BookingRepository;
import com.re.busticket.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String APIKEY_PREFIX = "Apikey ";
    private static final Pattern CONTENT_PATTERN = Pattern.compile("CK (\\d+)");

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;

    public boolean validateToken(String authorizationHeader, String expectedToken) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return false;
        }
        if (!authorizationHeader.startsWith(APIKEY_PREFIX)) {
            return false;
        }
        String extractedToken = authorizationHeader.substring(APIKEY_PREFIX.length());
        return extractedToken.equals(expectedToken);
    }

    public Long parseBookingIdFromContent(String content) {
        if (content == null || content.isEmpty()) {
            return null;
        }
        Matcher matcher = CONTENT_PATTERN.matcher(content);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    @Transactional
    public boolean confirmPayment(Long bookingId, Long transferAmount) {
        if (bookingId == null || transferAmount == null) {
            return false;
        }

        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            return false;
        }

        Booking booking = optionalBooking.get();
        if (booking.getBookingStatus() != BookingStatus.PENDING) {
            return false;
        }


        if (transferAmount != (long) booking.getPaymentAmount()) {
            return false;
        }


        booking.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        Optional<Seat> optionalSeat = seatRepository.findById(booking.getSeatId());
        if (optionalSeat.isPresent()) {
            Seat seat = optionalSeat.get();
            if (seat.getStatus() == SeatStatus.PENDING) {
                seat.setStatus(SeatStatus.BOOKED);
                seatRepository.save(seat);
            }
        }

        return true;
    }
}
