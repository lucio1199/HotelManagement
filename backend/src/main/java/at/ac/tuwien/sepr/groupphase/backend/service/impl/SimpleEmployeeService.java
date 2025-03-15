package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EmployeeMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Employee;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.EmployeeRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.EmployeeService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.EmployeeValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SimpleEmployeeService implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmployeeValidator employeeValidator;

    public SimpleEmployeeService(
        EmployeeRepository employeeRepository,
        EmployeeMapper employeeMapper,
        PasswordEncoder passwordEncoder,
        EmployeeValidator employeeValidator) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.passwordEncoder = passwordEncoder;
        this.employeeValidator = employeeValidator;
    }

    @Override
    public List<EmployeeListDto> findAll() {
        log.debug("Find all employees");

        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
            .map(employeeMapper::employeeToEmployeeListDto)
            .toList();
    }


    @Override
    public EmployeeListDto create(EmployeeCreateDto employeeCreateDto) throws ValidationException {
        log.debug("Create employee: {}", employeeCreateDto);
        log.debug("Employee create DTO: {}", employeeCreateDto);

        employeeValidator.validateForCreate(employeeCreateDto);

        Employee employee = employeeMapper.employeeCreateDtoToEmployee(employeeCreateDto);

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        Employee savedEmployee = employeeRepository.save(employee);
        return employeeMapper.employeeToEmployeeListDto(savedEmployee);
    }

    @Override
    public EmployeeDetailDto update(Long id, EmployeeUpdateDto employeeUpdateDto) throws ValidationException {
        log.debug("Updating employee with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("Employee id cannot be null");
        }

        employeeValidator.validateForUpdate(employeeUpdateDto);

        Employee existingEmployee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Employee with id " + id + " not found"));

        if (employeeUpdateDto.firstName() != null) {
            existingEmployee.setFirstName(employeeUpdateDto.firstName());
        }
        if (employeeUpdateDto.lastName() != null) {
            existingEmployee.setLastName(employeeUpdateDto.lastName());
        }
        if (employeeUpdateDto.phoneNumber() != null) {
            existingEmployee.setPhoneNumber(employeeUpdateDto.phoneNumber());
        }
        if (employeeUpdateDto.email() != null) {
            existingEmployee.setEmail(employeeUpdateDto.email());
        }
        if (employeeUpdateDto.roleType() != null) {
            existingEmployee.setRoleType(employeeUpdateDto.roleType());
        }
        if (employeeUpdateDto.password() != null) {
            existingEmployee.setPassword(passwordEncoder.encode(employeeUpdateDto.password()));
        }
        if (employeeUpdateDto.password() != null && !employeeUpdateDto.password().isEmpty()) {
            existingEmployee.setPassword(passwordEncoder.encode(employeeUpdateDto.password()));
        }
        if (employeeUpdateDto.password() != null) {
            existingEmployee.setPassword(passwordEncoder.encode(employeeUpdateDto.password()));
        }
        if (employeeUpdateDto.password() != null && !employeeUpdateDto.password().isEmpty()) {
            existingEmployee.setPassword(passwordEncoder.encode(employeeUpdateDto.password()));
        }


        Employee updatedEmployee = employeeRepository.save(existingEmployee);
        log.info("Employee with id {} updated successfully", id);

        return employeeMapper.employeeToEmployeeDetailDto(updatedEmployee);
    }

    @Override
    public EmployeeDetailDto findOne(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Employee with id " + id + " not found"));

        return employeeMapper.employeeToEmployeeDetailDto(employee);
    }

    @Override
    public void delete(Long id) {
        log.debug("Delete employee with id: {}", id);

        if (id == null) {
            throw new IllegalArgumentException("Employee id cannot be null");
        }
        Employee employeeToBeDeleted = employeeRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Employee with id " + id + " not found"));

        if (employeeToBeDeleted.getRoleType() == RoleType.ROLE_ADMIN) {
            throw new NotFoundException("Employee with id " + id + " not found");
        }

        employeeRepository.deleteById(id);
        log.info("Employee with id {} deleted successfully", id);
    }
}
