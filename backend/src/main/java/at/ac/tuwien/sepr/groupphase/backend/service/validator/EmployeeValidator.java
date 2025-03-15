package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class EmployeeValidator {

    /**
     * Validates the {@link EmployeeCreateDto} before creating a new employee.
     * Ensures that all required fields are valid.
     *
     * @param employeeCreateDto the {@link EmployeeCreateDto} to be validated.
     * @throws ValidationException if the validation fails.
     */
    public void validateForCreate(EmployeeCreateDto employeeCreateDto) throws ValidationException {
        log.trace("validateForCreate({})", employeeCreateDto);
        List<String> validationErrors = new ArrayList<>();

        // First Name Validation
        if (employeeCreateDto.firstName() == null || employeeCreateDto.firstName().isBlank()) {
            validationErrors.add("First name must not be null or blank.");
        } else if (employeeCreateDto.firstName().length() < 2 || employeeCreateDto.firstName().length() > 100) {
            validationErrors.add("First name must be between 2 and 100 characters.");
        }

        // Last Name Validation
        if (employeeCreateDto.lastName() == null || employeeCreateDto.lastName().isBlank()) {
            validationErrors.add("Last name must not be null or blank.");
        } else if (employeeCreateDto.lastName().length() < 2 || employeeCreateDto.lastName().length() > 100) {
            validationErrors.add("Last name must be between 2 and 100 characters.");
        }

        // Phone Number Validation (Optional Field)
        if (employeeCreateDto.phoneNumber() != null) {
            if (employeeCreateDto.phoneNumber().length() < 7 || employeeCreateDto.phoneNumber().length() > 15) {
                validationErrors.add("Phone number must be between 7 and 15 characters.");
            }
        }

        // Email Validation
        if (employeeCreateDto.email() == null || employeeCreateDto.email().isBlank()) {
            validationErrors.add("Email must not be null or blank.");
        } else if (!employeeCreateDto.email().matches("^[\\w-_.+]*[\\w-_.]@[\\w]+[.][a-z]+$")) {
            validationErrors.add("Email format is invalid.");
        } else if (employeeCreateDto.email().length() > 255) {
            validationErrors.add("Email must not exceed 255 characters.");
        }

        // Password Validation
        if (employeeCreateDto.password() == null || employeeCreateDto.password().isBlank()) {
            validationErrors.add("Password must not be null or blank.");
        } else if (employeeCreateDto.password().length() < 6) {
            validationErrors.add("Password must be at least 6 characters long.");
        } else if (employeeCreateDto.password().length() > 30) {
            validationErrors.add("Password cannot exceed 30 characters.");
        }

        // Role Type Validation
        if (employeeCreateDto.roleType() == null) {
            validationErrors.add("Role type must not be null.");
        } else if (employeeCreateDto.roleType() == RoleType.ROLE_ADMIN || employeeCreateDto.roleType() == RoleType.ROLE_GUEST) {
            validationErrors.add("Role type cannot be ROLE_ADMIN or ROLE_GUEST in create mode.");
        }

        // Throw exception if validation errors exist
        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }

    /**
     * Validates the {@link EmployeeUpdateDto} before updating an existing employee.
     * Ensures that all fields are manually validated.
     *
     * @param employeeUpdateDto the {@link EmployeeUpdateDto} to be validated.
     * @throws ValidationException if the validation fails.
     */
    public void validateForUpdate(EmployeeUpdateDto employeeUpdateDto) throws ValidationException {
        log.trace("validateForUpdate({})", employeeUpdateDto);
        List<String> validationErrors = new ArrayList<>();

        // First Name Validation
        if (employeeUpdateDto.firstName() != null && (employeeUpdateDto.firstName().length() < 2 || employeeUpdateDto.firstName().length() > 100)) {
            validationErrors.add("First name must be between 2 and 100 characters.");
        }

        // Last Name Validation
        if (employeeUpdateDto.lastName() != null && (employeeUpdateDto.lastName().length() < 2 || employeeUpdateDto.lastName().length() > 100)) {
            validationErrors.add("Last name must be between 2 and 100 characters.");
        }

        // Phone Number Validation (Optional)
        if (employeeUpdateDto.phoneNumber() != null && (employeeUpdateDto.phoneNumber().length() < 7 || employeeUpdateDto.phoneNumber().length() > 15)) {
            validationErrors.add("Phone number must be between 7 and 15 characters.");
        }

        // Email Validation
        if (employeeUpdateDto.email() != null && !employeeUpdateDto.email().matches("^[\\w-_.+]*[\\w-_.]@[\\w]+[.][a-z]+$")) {
            validationErrors.add("Email format is invalid.");
        }   else if (employeeUpdateDto.email().length() > 255) {
            validationErrors.add("Email must not exceed 255 characters.");
        }

        // Password Validation
        if (employeeUpdateDto.password() != null && employeeUpdateDto.password().length() < 6) {
            validationErrors.add("Password must be at least 6 characters long.");
        } else if (employeeUpdateDto.password().length() > 30) {
            validationErrors.add("Password cannot exceed 30 characters.");
        }

        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }
}

