package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;

/**
 * A DTO for creating and updating guests.
 */
public record GuestCreateUpdateDto(
    String firstName,
    String lastName,
    String email,
    LocalDate dateOfBirth,
    String placeOfBirth,
    String gender,
    String nationality,
    String address,
    String passportNumber,
    String phoneNumber,
    String password
) {
}
