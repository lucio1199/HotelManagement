package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * A Data Transfer Object (DTO) for user login details.
 *
 * @param email the email address of the user; must not be {@code null} and must follow a valid email format.
 * @param password the password of the user; must not be {@code null}.
 */
public record UserLoginDto(
    @NotNull(message = "Email must not be null")
    @Email
    String email,

    @NotNull(message = "Password must not be null")
    String password
) {}
