package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeListDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper
public interface EmployeeMapper {

    /**
     * Maps a single Employee entity to an EmployeeListDto.
     *
     * @param employee the Employee entity
     * @return the mapped EmployeeListDto
     */
    @Named("toEmployeeListDto")
    default EmployeeListDto employeeToEmployeeListDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        return new EmployeeListDto(
            employee.getId(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getPhoneNumber(),
            employee.getRoleType());
    }

    /**
     * Maps an EmployeeCreateDto to an Employee entity.
     *
     * @param employeeCreateDto the EmployeeCreateDto
     * @return the mapped Employee entity
     */
    default Employee employeeCreateDtoToEmployee(EmployeeCreateDto employeeCreateDto) {
        if (employeeCreateDto == null) {
            return null;
        }

        Employee employee = new Employee();
        employee.setFirstName(employeeCreateDto.firstName());
        employee.setLastName(employeeCreateDto.lastName());
        employee.setPhoneNumber(employeeCreateDto.phoneNumber());
        employee.setRoleType(employeeCreateDto.roleType());
        employee.setEmail(employeeCreateDto.email());
        employee.setPassword(employeeCreateDto.password());
        return employee;
    }


    /**
     * Maps an Employee entity to an EmployeeDetailDto.
     *
     * @param employee the Employee entity
     * @return the mapped EmployeeDetailDto
     */
    @Named("toEmployeeDetailDto")
    default EmployeeDetailDto employeeToEmployeeDetailDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        return new EmployeeDetailDto(
            employee.getEmail(),
            employee.getPassword(),
            employee.getFirstName(),
            employee.getLastName(),
            employee.getPhoneNumber(),
            employee.getRoleType());
    }
}
