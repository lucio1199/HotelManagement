package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;

/**
 * A DTO used for listing employees.
 *
 * @param id of the employee
 * @param firstName of the employee
 * @param lastName of the employee
 * @param phoneNumber of the employee
 * @param roleType of the employee
 */
public record EmployeeListDto(
    Long id,
    String firstName,
    String lastName,
    String phoneNumber,
    RoleType roleType
) {
}
