package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * A data transfer object for representing PDF-related information.
 * Contains details about a PDF document related to a booking.
 *
 * <p>This DTO includes the unique identifier for the PDF, the associated booking ID, and the document type.</p>
 *
 * @param id the unique identifier for the PDF document
 * @param bookingId the ID of the booking associated with the PDF
 * @param documentType the type of the document (e.g., "invoice", "cancellation receipt")
 */
public record PdfDto(
    Long id,
    Long bookingId,
    String documentType
) {}