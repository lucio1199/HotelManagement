package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleMailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;


import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private SimpleMailService mailService;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(mailService, "javaMailSender", javaMailSender);
        ReflectionTestUtils.setField(mailService, "mailIntegrationId", "testemail@example.com");
    }

    /**
     * Test case to verify that an email is sent when a valid booking is provided.
     */
    @Test
    public void givenValidBooking_whenSendEmail_thenEmailSent() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        Guest guest = new Guest();
        guest.setLastName("Doe");
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(guest);
        booking.setRoom(room);

        Pdf pdf = new Pdf();
        pdf.setDocumentType("Booking Confirmation");
        pdf.setContent("dummy-pdf-content".getBytes());

        mailService.sendEmail(booking, List.of(pdf));

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());

        MimeMessage capturedMessage = captor.getValue();
        assertEquals(mimeMessage, capturedMessage);
    }

    /**
     * Test case to verify that no email is sent when the mail integration ID is invalid.
     */
    @Test
    public void givenInvalidMailIntegrationId_whenSendEmail_thenNotSent() throws MessagingException {
        ReflectionTestUtils.setField(mailService, "mailIntegrationId", "");

        Guest guest = new Guest();
        guest.setLastName("Doe");
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(guest);
        booking.setRoom(room);

        Pdf pdf = new Pdf();
        pdf.setDocumentType("Booking Confirmation");
        pdf.setContent("dummy-pdf-content".getBytes());

        mailService.sendEmail(booking, List.of(pdf));

        verify(javaMailSender, times(0)).send(any(MimeMessage.class));
    }

    /**
     * Test case to verify that no email is sent when there is no recipient email address in the booking.
     */
    @Test
    public void givenNoRecipientEmail_whenSendEmail_thenNotSent() throws MessagingException {
        Guest guest = new Guest();
        guest.setLastName("Doe");
        guest.setEmail(null);

        Room room = new Room();
        room.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(guest);
        booking.setRoom(room);

        Pdf pdf = new Pdf();
        pdf.setDocumentType("Booking Confirmation");
        pdf.setContent("dummy-pdf-content".getBytes());

        mailService.sendEmail(booking, List.of(pdf));

        verify(javaMailSender, times(0)).send(any(MimeMessage.class));
    }

    /**
     * Test case to verify that an email is sent even when no attachments are provided in the booking.
     */
    @Test
    public void givenBookingWithoutAttachments_whenSendEmail_thenEmailSent() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        Guest guest = new Guest();
        guest.setLastName("Doe");
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(guest);
        booking.setRoom(room);

        mailService.sendEmail(booking, null);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());

        MimeMessage capturedMessage = captor.getValue();
        assertEquals(mimeMessage, capturedMessage);
    }

    /**
     * Test case to verify that a cancellation email is sent when a valid booking with cancellation details is provided.
     */
    @Test
    public void givenValidBooking_whenSendCancellationEmail_thenEmailSent() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        Guest guest = new Guest();
        guest.setLastName("Doe");
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(guest);
        booking.setRoom(room);
        booking.setCancellationDate(LocalDate.parse("2025-01-24"));

        byte[] cancellationPdf = "dummy-pdf-content".getBytes();

        mailService.sendCancellationEmail(booking, cancellationPdf);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(javaMailSender, times(1)).send(captor.capture());

        MimeMessage capturedMessage = captor.getValue();
        assertEquals(mimeMessage, capturedMessage);
    }

    /**
     * Test case to verify that no cancellation email is sent when the mail integration ID is invalid.
     */
    @Test
    public void givenInvalidMailIntegrationId_whenSendCancellationEmail_thenNotSent() throws MessagingException {
        ReflectionTestUtils.setField(mailService, "mailIntegrationId", "");

        Guest guest = new Guest();
        guest.setLastName("Doe");
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(guest);
        booking.setRoom(room);
        booking.setCancellationDate(LocalDate.parse("2025-01-24"));

        byte[] cancellationPdf = "dummy-pdf-content".getBytes();

        mailService.sendCancellationEmail(booking, cancellationPdf);

        verify(javaMailSender, times(0)).send(any(MimeMessage.class));
    }
}
