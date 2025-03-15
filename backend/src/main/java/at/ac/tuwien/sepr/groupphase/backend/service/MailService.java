package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InviteToRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import jakarta.mail.MessagingException;

import java.util.List;

public interface MailService {

    /**
     * Sends an email containing booking details and attached PDF documents.
     *
     * @param booking     The booking for which the email is being sent.
     * @param pdfDocuments A list of PDFs to be attached to the email.
     * @throws MessagingException If there is an error during the email sending process.
     */
    void sendEmail(Booking booking, List<Pdf> pdfDocuments) throws MessagingException;

    /**
     * Sends a mail to the recipient of an invitation.
     *
     * @param dto The details of the invitation.
     *
     * @throws MessagingException If there was an error sending the message.
     */
    void sendAddToRoomEmail(InviteToRoomDto dto) throws MessagingException;
}

