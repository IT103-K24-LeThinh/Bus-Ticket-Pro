package com.re.busticket.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendBookingConfirmationEmail(String toEmail, String passengerName,
                                              String bookingReference, String routeName,
                                              LocalDateTime departureTime, LocalDateTime arrivalTime,
                                              String seatNumber, String busLicensePlate,
                                              double paymentAmount, String paymentMethod) {
        String subject = "Xác nhận đặt vé thành công - " + bookingReference;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin:0;padding:0;font-family:Arial,sans-serif;background-color:#f4f4f4;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:600px;margin:20px auto;background:#ffffff;border-radius:8px;overflow:hidden;box-shadow:0 2px 8px rgba(0,0,0,0.1);">
                        <tr>
                            <td style="background-color:#4CAF50;padding:24px 30px;text-align:center;">
                                <h1 style="color:#ffffff;margin:0;font-size:22px;">Đặt vé thành công!</h1>
                            </td>
                        </tr>
                        <tr>
                            <td style="padding:30px;">
                                <p style="font-size:16px;color:#333;margin:0 0 20px;">
                                    Xin chào <strong>%s</strong>,
                                </p>
                                <p style="font-size:15px;color:#555;margin:0 0 24px;">
                                    Vé của bạn đã được xác nhận thành công. Dưới đây là thông tin chi tiết:
                                </p>
                                <table width="100%%" cellpadding="8" cellspacing="0" style="border:1px solid #e0e0e0;border-radius:6px;font-size:14px;color:#333;">
                                    <tr style="background:#f9f9f9;">
                                        <td style="font-weight:bold;width:40%%;border-bottom:1px solid #e0e0e0;">Mã vé</td>
                                        <td style="border-bottom:1px solid #e0e0e0;color:#4CAF50;font-weight:bold;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="font-weight:bold;border-bottom:1px solid #e0e0e0;">Tuyến đường</td>
                                        <td style="border-bottom:1px solid #e0e0e0;">%s</td>
                                    </tr>
                                    <tr style="background:#f9f9f9;">
                                        <td style="font-weight:bold;border-bottom:1px solid #e0e0e0;">Khởi hành</td>
                                        <td style="border-bottom:1px solid #e0e0e0;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="font-weight:bold;border-bottom:1px solid #e0e0e0;">Đến nơi</td>
                                        <td style="border-bottom:1px solid #e0e0e0;">%s</td>
                                    </tr>
                                    <tr style="background:#f9f9f9;">
                                        <td style="font-weight:bold;border-bottom:1px solid #e0e0e0;">Số ghế</td>
                                        <td style="border-bottom:1px solid #e0e0e0;font-weight:bold;">%s</td>
                                    </tr>
                                    <tr>
                                        <td style="font-weight:bold;border-bottom:1px solid #e0e0e0;">Biển số xe</td>
                                        <td style="border-bottom:1px solid #e0e0e0;">%s</td>
                                    </tr>
                                    <tr style="background:#f9f9f9;">
                                        <td style="font-weight:bold;border-bottom:1px solid #e0e0e0;">Số tiền</td>
                                        <td style="border-bottom:1px solid #e0e0e0;color:#E65100;font-weight:bold;">%,.0f VND</td>
                                    </tr>
                                    <tr>
                                        <td style="font-weight:bold;">Phương thức thanh toán</td>
                                        <td>%s</td>
                                    </tr>
                                </table>
                                <p style="font-size:14px;color:#777;margin:24px 0 0;line-height:1.5;">
                                    Vui lòng đến trước giờ khởi hành 15 phút. Chúc bạn có chuyến đi vui vẻ!
                                </p>
                            </td>
                        </tr>
                        <tr>
                            <td style="background:#f9f9f9;padding:16px 30px;text-align:center;font-size:12px;color:#999;border-top:1px solid #e0e0e0;">
                                &copy; 2026 Bus Ticket Pro. All rights reserved.
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(
                passengerName,
                bookingReference,
                routeName,
                departureTime != null ? departureTime.format(fmt) : "N/A",
                arrivalTime != null ? arrivalTime.format(fmt) : "N/A",
                seatNumber,
                busLicensePlate,
                paymentAmount,
                paymentMethod
        );

        sendHtmlMail(toEmail, subject, html);
    }

    public void sendHtmlMail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
