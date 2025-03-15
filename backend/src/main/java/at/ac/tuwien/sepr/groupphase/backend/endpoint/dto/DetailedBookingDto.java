package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;

/**
 * A detailed data transfer object for bookings.
 * Represents booking details, including room and user information.
 *
 * <p>This DTO contains comprehensive information about a booking, including booking and room IDs, user details, payment status, booking dates, price, and more.</p>
 *
 * @param id the unique identifier for the booking
 * @param roomId the ID of the room booked
 * @param userId the ID of the user who made the booking
 * @param startDate the start date of the booking
 * @param endDate the end date of the booking
 * @param roomName the name of the booked room
 * @param price the total price for the booking
 * @param isPaid indicates whether the booking has been paid for
 * @param status the current status of the booking (e.g., "active", "cancelled")
 * @param bookingNumber a unique reference number for the booking
 * @param bookingDate the date when the booking was made
 * @param totalAmount the total amount of the booking, including any applicable fees
 * @param numberOfNights the total number of nights booked
 */
public record DetailedBookingDto(
    Long id,
    Long roomId,
    Long userId,
    LocalDate startDate,
    LocalDate endDate,
    String roomName,
    Double price,
    Boolean isPaid,
    String status,
    String bookingNumber,
    LocalDate bookingDate,
    Double totalAmount,
    Integer numberOfNights,
    String paymentId
) {
}

