package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import java.io.IOException;

public interface PdfGenerationService {

    /**
     * Generates a PDF for the booking confirmation.
     *
     * @param booking The booking for which the confirmation is being generated.
     * @return A byte array representing the generated booking confirmation PDF.
     * @throws IOException If there is an error during the PDF generation process.
     */
    byte[] generateBookingConfirmation(Booking booking) throws IOException;

    /**
     * Generates a PDF invoice for the booking.
     *
     * @param booking The booking for which the invoice is being generated.
     * @return A byte array representing the generated invoice PDF.
     * @throws IOException If there is an error during the PDF generation process.
     */
    byte[] generateInvoice(Booking booking) throws IOException;

    /**
     * Generates a PDF for the booking cancellation receipt.
     *
     * @param booking The booking for which the cancellation receipt is being generated.
     * @return A byte array representing the generated cancellation receipt PDF.
     * @throws IOException If there is an error during the PDF generation process.
     */
    byte[] generateCancellation(Booking booking) throws IOException;
}
