package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * A data transfer object for getting the check-in information of a guest.
 *
 * @param bookingId the ID of the booking.
 * @param email the email of the guest.
 */
public record CheckInStatusDto(
    Long bookingId,
    String email
) {
}
