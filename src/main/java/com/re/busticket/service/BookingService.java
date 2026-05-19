package com.re.busticket.service;

import com.re.busticket.dto.BookingFormDto;
import com.re.busticket.entity.Booking;
import com.re.busticket.entity.Seat;
import com.re.busticket.entity.Trip;
import com.re.busticket.entity.enums.BookingStatus;
import com.re.busticket.entity.enums.PaymentMethod;
import com.re.busticket.entity.enums.SeatStatus;
import com.re.busticket.repository.BookingRepository;
import com.re.busticket.repository.SeatRepository;
import com.re.busticket.repository.TripRepository;
import com.re.busticket.service.exception.BookingNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final String REFERENCE_PREFIX = "BK-";

    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;

    @Transactional
    public Booking createBooking(BookingFormDto form, Long currentUserId) {
        Seat seat = seatRepository.findById(form.getSeatId())
                .orElseThrow(() -> new IllegalStateException("Ghế không còn khả dụng, vui lòng chọn ghế khác"));

        if (seat.getTripId() == null
                || !seat.getTripId().equals(form.getTripId())
                || seat.getStatus() != SeatStatus.AVAILABLE) {
            throw new IllegalStateException("Ghế không còn khả dụng, vui lòng chọn ghế khác");
        }

        Trip trip = tripRepository.findById(form.getTripId())
                .orElseThrow(() -> new IllegalStateException("Ghế không còn khả dụng, vui lòng chọn ghế khác"));

        PaymentMethod paymentMethod = form.getPaymentMethod();
        boolean isCash = paymentMethod == PaymentMethod.CASH;

        Booking booking = new Booking();
        booking.setUserId(currentUserId);
        booking.setTripId(form.getTripId());
        booking.setSeatId(form.getSeatId());
        booking.setBookingTime(LocalDateTime.now());
        Double ticketPrice = trip.getTicketPrice();
        booking.setPaymentAmount(ticketPrice != null ? ticketPrice : 0d);
        booking.setPaymentMethod(paymentMethod);
        booking.setBookingStatus(isCash ? BookingStatus.CONFIRMED : BookingStatus.PENDING);

        seat.setStatus(isCash ? SeatStatus.BOOKED : SeatStatus.PENDING);

        Booking saved = bookingRepository.save(booking);
        seatRepository.save(seat);
        return saved;
    }

    @Transactional
    public void cancelBooking(Long bookingId, Long currentUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AccessDeniedException("Bạn không có quyền huỷ vé này"));

        if (booking.getUserId() != currentUserId) {
            throw new AccessDeniedException("Bạn không có quyền huỷ vé này");
        }

        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Vé đã bị huỷ trước đó");
        }

        Trip trip = tripRepository.findById(booking.getTripId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy chuyến xe của vé này"));

        if (trip.getDepartureTime() == null
                || !trip.getDepartureTime().isAfter(LocalDateTime.now().plusHours(12))) {
            throw new IllegalStateException("Chỉ có thể huỷ vé ít nhất 12 giờ trước giờ khởi hành");
        }

        Seat seat = seatRepository.findById(booking.getSeatId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy ghế của vé này"));

        booking.setBookingStatus(BookingStatus.CANCELLED);
        seat.setStatus(SeatStatus.AVAILABLE);

        bookingRepository.save(booking);
        seatRepository.save(seat);
    }

    public boolean isCancellable(Booking booking, Trip trip) {
        return (booking.getBookingStatus() == BookingStatus.CONFIRMED
                || booking.getBookingStatus() == BookingStatus.PENDING)
                && trip.getDepartureTime() != null
                && trip.getDepartureTime().isAfter(LocalDateTime.now().plusHours(12));
    }

    public Page<Booking> findByUserId(Long userId, BookingStatus filterStatus, Pageable pageable) {
        if (filterStatus == null) {
            return bookingRepository.findByUserIdOrderByBookingTimeDesc(userId, pageable);
        }
        return bookingRepository.findByUserIdAndBookingStatusOrderByBookingTimeDesc(userId, filterStatus, pageable);
    }

    public Booking findOwnedById(Long bookingId, Long currentUserId) {
        return bookingRepository.findByIdAndUserId(bookingId, currentUserId)
                .orElseThrow(() -> new BookingNotFoundException("Không tìm thấy vé"));
    }

    public Optional<Booking> lookupByReference(String reference, Long currentUserId) {
        Long bookingId = parseReference(reference);
        if (bookingId == null) {
            return Optional.empty();
        }
        return bookingRepository.findByIdAndUserId(bookingId, currentUserId);
    }

    public String toReference(Booking booking) {
        return REFERENCE_PREFIX + booking.getId();
    }

    public Long parseReference(String reference) {
        if (reference == null) {
            return null;
        }
        String trimmed = reference.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String numericPart = trimmed;
        if (trimmed.regionMatches(true, 0, REFERENCE_PREFIX, 0, REFERENCE_PREFIX.length())) {
            numericPart = trimmed.substring(REFERENCE_PREFIX.length());
        }
        try {
            return Long.parseLong(numericPart);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
