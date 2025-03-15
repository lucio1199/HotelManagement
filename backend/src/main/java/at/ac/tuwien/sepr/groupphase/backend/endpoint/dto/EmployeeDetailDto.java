package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;

/**
 * A DTO representing the details of an employee.
 *
 * @param email the email of the employee
 * @param password the password of the employee
 * @param firstName the first name of the employee
 * @param lastName the last name of the employee
 * @param phoneNumber the phone number of the employee (optional)
 * @param roleType the role type of the employee
 */
public record EmployeeDetailDto(
    String email,
    String password,
    String firstName,
    String lastName,
    String phoneNumber,
    RoleType roleType
) {
}

