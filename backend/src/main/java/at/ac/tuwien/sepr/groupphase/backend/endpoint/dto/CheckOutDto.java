package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * A data transfer object for checking out a guest.
 * Represents the necessary information for checking out.
 *
 * @param bookingId the ID of the booking.
 * @param email the email of the guest.
 */
public record CheckOutDto(
    Long bookingId,
    String email
) {
}
