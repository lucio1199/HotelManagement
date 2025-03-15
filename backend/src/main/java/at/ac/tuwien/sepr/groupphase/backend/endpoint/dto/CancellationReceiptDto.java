package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;

/**
 * A data transfer object for representing the cancellation receipt of a booking.
 * Contains information about the cancelled booking and the refund details.
 *
 * <p>This DTO includes the booking ID, booking number, room details, cancellation date, and the refund amount issued.</p>
 *
 * @param bookingId the unique identifier for the cancelled booking
 * @param bookingNumber the booking reference number
 * @param roomName the name of the room that was booked
 * @param startDate the start date of the original booking
 * @param endDate the end date of the original booking
 * @param cancellationDate the date when the booking was cancelled
 * @param refundAmount the amount refunded for the cancellation
 */
public record CancellationReceiptDto(
    Long bookingId,
    String bookingNumber,
    String roomName,
    LocalDate startDate,
    LocalDate endDate,
    LocalDate cancellationDate,
    Double refundAmount
) {
}