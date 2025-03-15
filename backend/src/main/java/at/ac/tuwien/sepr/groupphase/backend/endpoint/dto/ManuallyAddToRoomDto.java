package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * A data transfer object for manually adding a guest to a room.
 *
 * @param bookingId the ID of the booking.
 * @param email the email of the guest.
 */
public record ManuallyAddToRoomDto(
    Long bookingId,
    String email
) {
}
