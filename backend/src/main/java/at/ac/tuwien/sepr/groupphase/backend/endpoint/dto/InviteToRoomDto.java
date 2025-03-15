package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;

import java.time.LocalDate;

/**
 * A data transfer object for inviting a guest to a room.
 * Represents the necessary information for marking them to be able to check in.
 *
 * @param bookingId the ID of the booking.
 * @param ownerEmail the email of the guest that owns the room.
 * @param email the email of the guest.
 */
public record InviteToRoomDto(
    Long bookingId,
    String email,
    String ownerEmail
) {
}
