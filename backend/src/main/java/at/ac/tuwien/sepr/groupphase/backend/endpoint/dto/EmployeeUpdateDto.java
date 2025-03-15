package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;

/**
 * A DTO used for updating employees.
 *
 * @param firstName the updated first name of the employee (optional)
 * @param lastName the updated last name of the employee (optional)
 * @param phoneNumber the updated phone number of the employee (optional)
 * @param roleType the updated role type of the employee (optional)
 * @param email the updated email address of the employee (optional)
 * @param password the updated password of the employee (optional)
 */
public record EmployeeUpdateDto(
    String firstName,
    String lastName,
    String phoneNumber,
    RoleType roleType,
    String email,
    String password
) {
}
