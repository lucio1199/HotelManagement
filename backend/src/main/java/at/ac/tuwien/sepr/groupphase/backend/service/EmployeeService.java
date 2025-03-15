package at.ac.tuwien.sepr.groupphase.backend.service;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeListDto;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Employee;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;

import java.util.List;

public interface EmployeeService {

    /**
     * Retrieves all rooms from the repository, ordered by price in ascending order.
     *
     * @return a list of all {@link EmployeeListDto}, sorted by last name in descending order.
     */
    List<EmployeeListDto> findAll();


    /**
     * Creates a new employee based on the provided EmployeeCreateDto.
     *
     * @param employeeCreateDto the DTO containing employee creation details
     * @return the created {@link EmployeeListDto}
     */
    EmployeeListDto create(EmployeeCreateDto employeeCreateDto) throws ValidationException;

    /**
     * Updates an existing employee with the specified ID using the provided update data.
     *
     * <p>Only the fields that are present in the {@code EmployeeUpdateDto} will be updated.
     * Fields that are {@code null} in the DTO will remain unchanged. If a password is provided,
     * it will be securely encoded before being updated.</p>
     *
     * @param id the ID of the employee to update; must not be {@code null}.
     * @param employeeUpdateDto the DTO containing the updated fields for the employee; must not be {@code null}.
     * @return an {@code EmployeeDetailDto} representing the updated employee.
     * @throws IllegalArgumentException if the {@code id} is {@code null}.
     * @throws NotFoundException if no employee with the specified {@code id} exists in the repository.
     */
    EmployeeDetailDto update(Long id, EmployeeUpdateDto employeeUpdateDto) throws NotFoundException, ValidationException;

    /**
     * Finds an employee by their id.
     *
     * @param id of the employee
     * @return the {@link EmployeeDetailDto} of the found @{@link Employee}
     */
    EmployeeDetailDto findOne(Long id);


    /**
     * Deletes an employee by their id.
     *
     * @param id of the employee to be deleted
     */
    void delete(Long id);
}
