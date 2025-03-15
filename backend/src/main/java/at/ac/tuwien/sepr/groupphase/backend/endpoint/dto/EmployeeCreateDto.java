package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;

/**
 * A DTO used for creating employees.
 *
 * @param firstName of the employee
 * @param lastName of the employee
 * @param phoneNumber of the employee
 * @param roleType of the employee
 * @param email of the employee
 * @param password of the employee
 */
public record EmployeeCreateDto(
    String firstName,
    String lastName,
    String phoneNumber,
    RoleType roleType,
    String email,
    String password
) {
}

