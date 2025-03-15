package at.ac.tuwien.sepr.groupphase.backend.unittests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

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
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleEmployeeService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.EmployeeValidator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmployeeValidator employeeValidator;

    @InjectMocks
    private SimpleEmployeeService employeeService;

    @Test
    public void givenEmployee_whenCreate_thenSaveAndReturnEmployeeListDto() throws Exception {
        // Arrange
        EmployeeCreateDto employeeCreateDto = new EmployeeCreateDto("John", "Doe", "123456789", RoleType.ROLE_CLEANING_STAFF, "john.doe@example.com", "password123");
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setPhoneNumber("123456789");
        employee.setEmail("john.doe@example.com");
        employee.setRoleType(RoleType.ROLE_CLEANING_STAFF);
        employee.setPassword("encodedPassword");

        EmployeeListDto expectedEmployeeListDto = new EmployeeListDto(1L, "John", "Doe", "123456789", RoleType.ROLE_CLEANING_STAFF);

        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(employeeMapper.employeeCreateDtoToEmployee(any(EmployeeCreateDto.class))).thenReturn(employee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employeeMapper.employeeToEmployeeListDto(any(Employee.class))).thenReturn(expectedEmployeeListDto);

        doNothing().when(employeeValidator).validateForCreate(any(EmployeeCreateDto.class));

        // Act
        EmployeeListDto savedEmployee = employeeService.create(employeeCreateDto);

        // Assert
        verify(employeeRepository, times(1)).save(any(Employee.class));
        assertNotNull(savedEmployee, "Saved employee should not be null");
        assertEquals("John", savedEmployee.firstName());
        assertEquals("Doe", savedEmployee.lastName());
        assertEquals("123456789", savedEmployee.phoneNumber());
        assertEquals(RoleType.ROLE_CLEANING_STAFF, savedEmployee.roleType());
    }

    @Test
    public void givenInvalidEmployeeId_whenUpdate_thenThrowNotFoundException() {
        // Arrange
        Long invalidId = 999L;
        EmployeeUpdateDto employeeUpdateDto = new EmployeeUpdateDto("John", null, null, null, null, null);

        when(employeeRepository.findById(eq(invalidId))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> employeeService.update(invalidId, employeeUpdateDto));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testFindAll() {
        // Arrange
        Employee employee1 = new Employee();
        employee1.setId(1L);
        employee1.setFirstName("John");
        employee1.setLastName("Doe");

        Employee employee2 = new Employee();
        employee2.setId(2L);
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");

        List<Employee> employees = List.of(employee1, employee2);

        when(employeeMapper.employeeToEmployeeListDto(employee1)).thenReturn(new EmployeeListDto(1L, "John", "Doe", "123456789", RoleType.ROLE_CLEANING_STAFF));
        when(employeeMapper.employeeToEmployeeListDto(employee2)).thenReturn(new EmployeeListDto(2L, "Jane", "Smith", "987654321", RoleType.ROLE_ADMIN));
        when(employeeRepository.findAll()).thenReturn(employees);

        List<EmployeeListDto> result = employeeService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John", result.get(0).firstName());
        assertEquals("Jane", result.get(1).firstName());
    }


    @Test
    void testUpdate() throws ValidationException {
        Long id = 1L;
        EmployeeUpdateDto updateDto = new EmployeeUpdateDto("John", "Doe", "987654321", RoleType.ROLE_ADMIN,"john.doe@newmail.com",  "newpassword");

        Employee existingEmployee = new Employee();
        existingEmployee.setFirstName("John");
        existingEmployee.setLastName("Doe");
        existingEmployee.setPhoneNumber("123456789");
        existingEmployee.setEmail("john.doe@example.com");
        existingEmployee.setRoleType(RoleType.ROLE_CLEANING_STAFF);
        existingEmployee.setPassword("encodedPassword");

        Employee updatedEmployee = new Employee();
        updatedEmployee.setFirstName("John");
        updatedEmployee.setLastName("Doe");
        updatedEmployee.setPhoneNumber("987654321");
        updatedEmployee.setEmail("john.doe@newmail.com");
        updatedEmployee.setRoleType(RoleType.ROLE_ADMIN);
        updatedEmployee.setPassword("encodedNewPassword"); // This should match the encoded value

        when(employeeRepository.findById(id)).thenReturn(Optional.of(existingEmployee));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedNewPassword");
        when(employeeRepository.save(existingEmployee)).thenReturn(updatedEmployee);
        when(employeeMapper.employeeToEmployeeDetailDto(updatedEmployee))
            .thenReturn(new EmployeeDetailDto("john.doe@newmail.com", "encodedNewPassword","John", "Doe", "987654321", RoleType.ROLE_ADMIN));

        EmployeeDetailDto result = employeeService.update(id, updateDto);

        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("987654321", result.phoneNumber());
        assertEquals("john.doe@newmail.com", result.email());
        assertEquals(RoleType.ROLE_ADMIN, result.roleType());
    }

    @Test
    void testFindOne() {
        Long id = 1L;
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setPhoneNumber("123456789");
        employee.setEmail("john.doe@example.com");
        employee.setRoleType(RoleType.ROLE_CLEANING_STAFF);
        employee.setPassword("encodedPassword");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(employeeMapper.employeeToEmployeeDetailDto(employee)).thenReturn(new EmployeeDetailDto("john.doe@example.com", "encodedPassword", "John", "Doe", "123456789",  RoleType.ROLE_CLEANING_STAFF));

        EmployeeDetailDto result = employeeService.findOne(id);

        assertNotNull(result);
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals("123456789", result.phoneNumber());
        assertEquals("john.doe@example.com", result.email());
        assertEquals(RoleType.ROLE_CLEANING_STAFF, result.roleType());
    }

    @Test
    void testDelete() {
        Long id = 1L;
        Employee employee = new Employee();
        employee.setFirstName("John");
        employee.setLastName("Doe");
        employee.setPhoneNumber("123456789");
        employee.setEmail("john.doe@example.com");
        employee.setRoleType(RoleType.ROLE_CLEANING_STAFF);
        employee.setPassword("encodedPassword");

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));

        employeeService.delete(id);

        verify(employeeRepository, times(1)).deleteById(id);
    }

    @Test
    void testUpdate_withNonExistingId() {
        Long invalidId = 999L;
        EmployeeUpdateDto updateDto = new EmployeeUpdateDto("John", "Doe", "987654321", RoleType.ROLE_ADMIN, "john.doe@example.com", "newpassword");

        when(employeeRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.update(invalidId, updateDto));
    }

    @Test
    void testFindOne_withNonExistingId() {
        Long invalidId = 999L;
        when(employeeRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.findOne(invalidId));
    }

    @Test
    void testDelete_withNonExistingId() {
        Long invalidId = 999L;
        when(employeeRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> employeeService.delete(invalidId));
    }
}


