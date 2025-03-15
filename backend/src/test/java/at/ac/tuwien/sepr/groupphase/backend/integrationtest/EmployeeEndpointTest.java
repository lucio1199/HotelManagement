package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;
import at.ac.tuwien.sepr.groupphase.backend.repository.EmployeeRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.EmployeeMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Employee;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;

import java.util.List;

import static at.ac.tuwien.sepr.groupphase.backend.basetest.TestData.ADMIN_USER;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the Employee endpoint, testing various CRUD operations
 * related to employee management, including retrieval and creation of employee data.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class EmployeeEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    @Qualifier("userDetailsServiceStub")
    private UserDetailsService userDetailsServiceStub;

    private static final String EMPLOYEE_BASE_URI = "/api/v1/employee";

    /**
     * Setup method to prepare the test environment before each test.
     * Clears the Employee repository to ensure a clean state.
     */
    @BeforeEach
    public void beforeEach() {
        employeeRepository.deleteAll();
    }

    /**
     * Test case: Successfully creates a new employee.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testCreateEmployee_Success() throws Exception {
        EmployeeCreateDto employeeCreateDto = new EmployeeCreateDto(
            "John",
            "Doe",
            "1234567890",
            RoleType.ROLE_CLEANING_STAFF,
            "john.doe@email.com",
            "StrongPass123"
        );

        mockMvc.perform(MockMvcRequestBuilders.post(EMPLOYEE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeCreateDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.phoneNumber").value("1234567890"))
            .andExpect(jsonPath("$.roleType").value("ROLE_CLEANING_STAFF"));
    }

    /**
     * Test case: Creation fails with invalid input.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testCreateEmployee_Failure_InvalidInput() throws Exception {
        EmployeeCreateDto invalidEmployee = new EmployeeCreateDto(
            "J", // too short
            "D", // too short
            "123", // too short
            RoleType.ROLE_ADMIN, // invalid role
            "invalid-email", // invalid email format
            "12345" // too short
        );

        mockMvc.perform(MockMvcRequestBuilders.post(EMPLOYEE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmployee)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testFindAllEmployees_Success() throws Exception {
        EmployeeCreateDto employeeCreateDto1 = new EmployeeCreateDto(
            "John",
            "Doe",
            "1234567890",
            RoleType.ROLE_CLEANING_STAFF,
            "john.doe@email.com",
            "StrongPass123"
        );
        EmployeeCreateDto employeeCreateDto2 = new EmployeeCreateDto(
            "Jane",
            "Doe",
            "0987654321",
            RoleType.ROLE_ADMIN,
            "jane.doe@email.com",
            "StrongPass123"
        );

        Employee employee1 = employeeMapper.employeeCreateDtoToEmployee(employeeCreateDto1);
        Employee employee2 = employeeMapper.employeeCreateDtoToEmployee(employeeCreateDto2);

        employeeRepository.save(employee1);
        employeeRepository.save(employee2);

        mockMvc.perform(MockMvcRequestBuilders.get(EMPLOYEE_BASE_URI)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("John"))
            .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testUpdateEmployee_Success() throws Exception {
        EmployeeCreateDto employeeCreateDto = new EmployeeCreateDto(
            "John",
            "Doe",
            "1234567890",
            RoleType.ROLE_CLEANING_STAFF,
            "john.doe@email.com",
            "StrongPass123"
        );
        Employee employee = employeeMapper.employeeCreateDtoToEmployee(employeeCreateDto);
        employeeRepository.save(employee);

        EmployeeUpdateDto employeeUpdateDto = new EmployeeUpdateDto(
            "John", // First name stays the same
            "Smith", // Last name changed
            "0987654321", // New phone number
            RoleType.ROLE_ADMIN,
            "john.smith@email.com", // New email
            "NewStrongPass123" // New password
        );

        mockMvc.perform(MockMvcRequestBuilders.put(EMPLOYEE_BASE_URI + "/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeUpdateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Smith"))
            .andExpect(jsonPath("$.phoneNumber").value("0987654321"))
            .andExpect(jsonPath("$.email").value("john.smith@email.com"))
            .andExpect(jsonPath("$.roleType").value("ROLE_ADMIN"));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testFindOneEmployee_Success() throws Exception {
        EmployeeCreateDto employeeCreateDto = new EmployeeCreateDto(
            "John",
            "Doe",
            "1234567890",
            RoleType.ROLE_CLEANING_STAFF,
            "john.doe@email.com",
            "StrongPass123"
        );
        Employee employee = employeeMapper.employeeCreateDtoToEmployee(employeeCreateDto);
        employeeRepository.save(employee);

        mockMvc.perform(MockMvcRequestBuilders.get(EMPLOYEE_BASE_URI + "/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("John"))
            .andExpect(jsonPath("$.lastName").value("Doe"))
            .andExpect(jsonPath("$.phoneNumber").value("1234567890"))
            .andExpect(jsonPath("$.email").value("john.doe@email.com"));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testDeleteEmployee_Success() throws Exception {
        EmployeeCreateDto employeeCreateDto = new EmployeeCreateDto(
            "John",
            "Doe",
            "1234567890",
            RoleType.ROLE_CLEANING_STAFF,
            "john.doe@email.com",
            "StrongPass123"
        );
        Employee employee = employeeMapper.employeeCreateDtoToEmployee(employeeCreateDto);
        employeeRepository.save(employee);

        mockMvc.perform(MockMvcRequestBuilders.delete(EMPLOYEE_BASE_URI + "/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        boolean exists = employeeRepository.existsById(employee.getId());
        assertFalse(exists);
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testDeleteEmployee_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(EMPLOYEE_BASE_URI + "/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testFindOneEmployee_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(EMPLOYEE_BASE_URI + "/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testUpdateEmployee_Failure_NotFound() throws Exception {
        EmployeeUpdateDto employeeUpdateDto = new EmployeeUpdateDto(
            "John",
            "Smith",
            "0987654321",
            RoleType.ROLE_ADMIN,
            "john.smith@email.com",
            "NewStrongPass123"
        );

        mockMvc.perform(MockMvcRequestBuilders.put(EMPLOYEE_BASE_URI + "/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeUpdateDto)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Employee with id 999 not found"));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testUpdateEmployee_Failure_InvalidInput() throws Exception {
        EmployeeCreateDto employeeCreateDto = new EmployeeCreateDto(
            "John",
            "Doe",
            "1234567890",
            RoleType.ROLE_CLEANING_STAFF,
            "john.doe@email.com",
            "StrongPass123"
        );
        Employee employee = employeeMapper.employeeCreateDtoToEmployee(employeeCreateDto);
        employeeRepository.save(employee);

        EmployeeUpdateDto invalidUpdateDto = new EmployeeUpdateDto(
            "J",
            null,
            null,
            null,
            "invalid-email",
            ""
        );

        mockMvc.perform(MockMvcRequestBuilders.put(EMPLOYEE_BASE_URI + "/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidUpdateDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors").isNotEmpty());
    }
}
