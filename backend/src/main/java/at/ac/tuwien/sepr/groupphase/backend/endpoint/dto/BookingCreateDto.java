package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;

/**
 * A data transfer object for creating a booking.
 * Represents the necessary information for booking a room.
 *
 * <p>This DTO contains the essential fields needed to create a booking, including room ID, start and end dates, and the chosen payment method.</p>
 *
 * @param roomId the ID of the room to be booked
 * @param startDate the start date of the booking period
 * @param endDate the end date of the booking period
 * @param paymentMethod the method of payment chosen for the booking
 */
public record BookingCreateDto(
    Long roomId,
    LocalDate startDate,
    LocalDate endDate,
    String paymentMethod
) {
}
