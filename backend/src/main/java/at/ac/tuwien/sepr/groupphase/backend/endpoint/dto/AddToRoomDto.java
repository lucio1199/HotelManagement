package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;

import java.time.LocalDate;

/**
 * A data transfer object for adding a guest to a room.
 * Represents the necessary information for checking them in.
 *
 * @param bookingId the ID of the booking.
 * @param firstName the first name of the guest.
 * @param lastName the last name of the guest.
 * @param dateOfBirth the birthdate of the guest.
 * @param placeOfBirth the place of birth of the guest.
 * @param gender the {@link Gender} of the guest.
 * @param nationality the {@link Nationality} of the guest.
 * @param address the address of the guest.
 * @param passportNumber the passport number of the guest.
 * @param phoneNumber the phone number of the guest.
 * @param email the email of the guest.
 */
public record AddToRoomDto(
    Long bookingId,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    String placeOfBirth,
    Gender gender,
    Nationality nationality,
    String address,
    String passportNumber,
    String phoneNumber,
    String email
) {
}
