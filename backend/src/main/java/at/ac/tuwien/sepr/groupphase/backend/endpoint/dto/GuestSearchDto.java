package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * A data transfer object for searching guests based on various criteria.
 *
 */
public record GuestSearchDto(
    String firstName,
    String lastName,
    String email
) {
}
