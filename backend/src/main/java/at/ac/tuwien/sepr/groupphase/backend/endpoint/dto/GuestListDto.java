package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * Data transfer object for guest list entries.
 */
public record GuestListDto(
    String firstName,
    String lastName,
    String email
) {
}
