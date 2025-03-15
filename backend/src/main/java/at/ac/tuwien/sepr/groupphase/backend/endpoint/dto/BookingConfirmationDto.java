package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;

/**
 * A data transfer object for confirming a booking.
 *
 * <p>This DTO contains the necessary details about a booking confirmation, including booking ID, room details, guest information, and the total price.</p>
 *
 * @param bookingId the unique identifier for the booking
 * @param roomId the unique identifier for the room being booked
 * @param roomName the name of the room being booked
 * @param startDate the start date of the booking
 * @param endDate the end date of the booking
 * @param guestFirstName the first name of the guest making the booking
 * @param guestLastName the last name of the guest making the booking
 * @param guestEmail the email address of the guest making the booking
 * @param totalPrice the total price for the booking
 */
public record BookingConfirmationDto(
    Long bookingId,
    Long roomId,
    String roomName,
    LocalDate startDate,
    LocalDate endDate,
    String guestFirstName,
    String guestLastName,
    String guestEmail,
    Double totalPrice
) {}

