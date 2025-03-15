package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;

public interface PdfStorageService {

    /**
     * Stores a PDF document associated with a booking.
     *
     * @param bookingId The ID of the booking to associate the PDF with.
     * @param type      The type of the document (e.g., "invoice", "confirmation").
     * @param content   The byte array content of the PDF document.
     */
    void storePdf(Long bookingId, String type, byte[] content);

    /**
     * Retrieves a PDF document associated with a booking.
     *
     * @param bookingId The ID of the booking to retrieve the PDF for.
     * @param type      The type of the document (e.g., "invoice", "confirmation").
     * @return The retrieved PDF entity.
     */
    Pdf getPdf(Long bookingId, String type);

    /**
     * Deletes a PDF document associated with a booking.
     *
     * @param bookingId The ID of the booking for which the PDF is to be deleted.
     * @param fileName  The name of the file to be deleted.
     */
    void deletePdf(Long bookingId, String fileName);
}