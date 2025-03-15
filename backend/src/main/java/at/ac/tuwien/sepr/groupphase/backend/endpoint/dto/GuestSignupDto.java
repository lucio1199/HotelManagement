package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * A DTO representing a guest. This should be expanded later as more functionality gets added
 *
 * @param email of the guest
 * @param password of the guest
 */
public record GuestSignupDto(
    String email,

    String password
) {}
