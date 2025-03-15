package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InviteToRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service(value = "SimpleMailService")
public class SimpleMailService implements MailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String mailIntegrationId;

    @Autowired
    public SimpleMailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public void sendEmail(Booking booking, List<Pdf> pdfDocuments) throws MessagingException {
        if (mailIntegrationId == null || mailIntegrationId.isBlank()) {
            log.error("MAIL_INTEGRATION_ID is not configured!");
            return;
        }

        String recipientEmail = booking.getUser() != null ? booking.getUser().getEmail() : null;
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.error("Recipient email is null or blank. Email cannot be sent.");
            return;
        }

        MimeMessageHelper helper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
        helper.setFrom(mailIntegrationId);
        helper.setTo(recipientEmail);
        helper.setSubject("Booking Confirmation");
        helper.setText(getEmailContent(booking), true);

        if (pdfDocuments != null && !pdfDocuments.isEmpty()) {
            for (Pdf pdf : pdfDocuments) {
                helper.addAttachment(pdf.getDocumentType(), new ByteArrayResource(pdf.getContent()));
            }
        }

        javaMailSender.send(helper.getMimeMessage());
        log.info("Email successfully sent to {}", recipientEmail);
    }


    private String getEmailContent(Booking booking) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<h1>Booking Confirmation</h1>")
            .append("<p>Dear Mr./Ms. ").append(booking.getGuest().getLastName()).append(",</p>")
            .append("<p>Thank you for choosing our hotel for your upcoming stay. It is our great pleasure to welcome you,"
                + "and we are delighted to confirm that your booking has been successfully processed.</p>")
            .append("<p>Your comfort and satisfaction are our top priorities, and we are committed to ensuring that your"
                + "experience with us is nothing short of exceptional. Below, you will find the details of your booking:</p>")
            .append("<h2>Booking Details</h2>")
            .append("<ul>")
            .append("<li><strong>Booking Number:</strong> ").append(booking.getBookingNumber()).append("</li>")
            .append("<li><strong>Check-in Date:</strong> ").append(booking.getStartDate()).append("</li>")
            .append("<li><strong>Check-out Date:</strong> ").append(booking.getEndDate()).append("</li>")
            .append("<li><strong>Room:</strong> ").append(booking.getRoom().getName()).append("</li>")
            .append("</ul>")
            .append("<p>Should you require any adjustments to your booking, please feel free to reach out to us, "
                + "and we will gladly assist you.</p>")
            .append("<h2>Cancellation Policy</h2>")
            .append("<p>We understand that plans can change. Should you need to cancel your reservation,"
                + "you can do so easily via the 'My Bookings' section on our website. Please note that cancellation "
                + "policies may vary depending on the terms of your booking.</p>")
            .append("<h2>Contact Us</h2>")
            .append("<p>We are here to assist you at every step of your journey. If you have any questions, "
                + "concerns, or special requests, please feel free to contact us at: <strong>inncontrolhotel@gmail.com</strong>. Our team is always ready to help.</p>")
            .append("<p>Once again, thank you for choosing our hotel. We look forward to providing you with a "
                + "memorable experience and warmly welcoming you to our property.</p>")
            .append("<p>Warm regards,</p>")
            .append("<p>The Hotel Team</p>");
        return emailContent.toString();
    }

    public void sendCancellationEmail(Booking booking, byte[] cancellationPdf) throws MessagingException {
        if (mailIntegrationId == null || mailIntegrationId.isBlank()) {
            log.error("MAIL_INTEGRATION_ID is not configured!");
            return;
        }

        String recipientEmail = booking.getUser().getEmail();
        MimeMessageHelper helper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);

        if (helper != null) {
            helper.setFrom(mailIntegrationId);
            helper.setTo(recipientEmail);
            helper.setSubject("Booking Cancellation");

            String emailContent = getCancellationEmailContent(booking);
            helper.setText(emailContent, true);

            helper.addAttachment("BookingCancellation.pdf", new ByteArrayResource(cancellationPdf));

            if (javaMailSender != null) {
                javaMailSender.send(helper.getMimeMessage());
                log.info("Cancellation email successfully sent to {}", recipientEmail);
            } else {
                log.error("JavaMailSender is not configured correctly.");
            }
        } else {
            log.error("Failed to create MimeMessageHelper for sending cancellation email to {}", recipientEmail);
        }
    }

    private String getCancellationEmailContent(Booking booking) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<h1>Booking Cancellation</h1>")
            .append("<p>Dear Mr./Ms. ").append(booking.getGuest().getLastName()).append(",</p>")
            .append("<p>We regret to inform you that your booking has been successfully cancelled. "
                + "Below are the details of your canceled reservation:</p>")
            .append("<h2>Cancellation Details</h2>")
            .append("<ul>")
            .append("<li><strong>Booking Number:</strong> ").append(booking.getBookingNumber()).append("</li>")
            .append("<li><strong>Room Name:</strong> ").append(booking.getRoom().getName()).append("</li>")
            .append("<li><strong>Booking Period:</strong> ").append(booking.getStartDate()).append(" to ").append(booking.getEndDate()).append("</li>")
            .append("<li><strong>Cancellation Date:</strong> ").append(booking.getCancellationDate()).append("</li>")
            .append("</ul>")
            .append("<p>We sincerely apologize for any inconvenience this may have caused and would like to "
                + "assure you that we value your patronage.</p>")
            .append("<p>We look forward to welcoming you back in the near future. Should you wish to book "
                + "with us again, we are always here to assist you and ensure an exceptional experience.</p>")
            .append("<h2>Contact Us</h2>")
            .append("<p>If you have any questions or need further assistance, please don’t hesitate to reach "
                + "out to us at <strong>inncontrolhotel@gmail.com</strong>.</p>")
            .append("<p>Thank you for considering our hotel, and we hope to host you again soon.</p>")
            .append("<p>Warm regards,</p>")
            .append("<p>The Hotel Team</p>");
        return emailContent.toString();
    }

    @Override
    public void sendAddToRoomEmail(InviteToRoomDto dto) throws MessagingException {
        if (mailIntegrationId == null || mailIntegrationId.isBlank()) {
            log.error("MAIL_INTEGRATION_ID is not configured!");
            return;
        }

        String recipientEmail = dto != null ? dto.email() : null;
        if (recipientEmail == null || recipientEmail.isBlank()) {
            log.error("Recipient email is null or blank. Email cannot be sent.");
            return;
        }

        MimeMessageHelper helper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
        helper.setFrom(mailIntegrationId);
        helper.setTo(recipientEmail);
        helper.setSubject("Invitation to Shared Room");
        helper.setText(getEmailContentAddToRoom(dto), true);

        javaMailSender.send(helper.getMimeMessage());
        log.info("Email successfully sent to {}", recipientEmail);
    }

    private String getEmailContentAddToRoom(InviteToRoomDto dto) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<h1>Invitation to Shared Room</h1>")
                .append("<p>Dear Mr./Ms.,</p>")
                .append("<p>we'd like to inform you, that you were invited to a hotel room by "
                        + dto.ownerEmail() + ".</p>")
                .append("<p>Your comfort and satisfaction are our top priorities, and we are committed to ensuring that your"
                        + " experience with us is nothing short of exceptional. Below, you will find the details to accept the invitation.</p>")
                .append("<h2>How to Accept the Invitation</h2>")
                .append("<p>Please click the button below to accept the invitation. When you are asked to input your personal information, "
                        + " please do so kindly. ")
                .append("It is also necessary that you upload your passport, as per Austria's regulations on hotel stays.</p>")
                .append("<a href='http://localhost:4200/#/check-in/").append(dto.bookingId()).append("'><button>Accept Invitation</button></a>")
                .append("<h2>Contact Us</h2>")
                .append("<p>We are here to assist you at every step of your journey. If you have any questions, "
                        + "concerns, or special requests, please feel free to contact us at: <strong>inncontrolhotel@gmail.com</strong>. Our team is always ready to help.</p>")
                .append("<p>Once again, thank you for choosing our hotel. We look forward to providing you with a "
                        + "memorable experience and warmly welcoming you to our property.</p>")
                .append("<p>Warm regards,</p>")
                .append("<p>The Hotel Team</p>");
        return emailContent.toString();
    }
}
