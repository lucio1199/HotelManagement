package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("api/v1/employee")
public class EmployeeEndpoint {
    private final EmployeeService employeeService;

    public EmployeeEndpoint(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Secured("ROLE_ADMIN")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<EmployeeListDto> findAll() {
        log.info("GET /api/v1/employee");
        return employeeService.findAll();
    }

    @Secured("ROLE_ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeListDto create(
        @Valid @RequestBody EmployeeCreateDto employeeDto) throws ValidationException, IOException {
        log.info("POST /api/v1/employee");
        log.debug("request body: {}", employeeDto);
        return employeeService.create(employeeDto);
    }

    @Secured("ROLE_ADMIN")
    @PutMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeDetailDto update(
        @PathVariable("id") Long id,
        @Valid @RequestBody EmployeeUpdateDto employeeUpdateDto) throws ValidationException {
        log.info("PUT /api/v1/employee/{}", id);
        log.debug("request body: {}", employeeUpdateDto);

        return employeeService.update(id, employeeUpdateDto);
    }

    @Secured("ROLE_ADMIN")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public EmployeeDetailDto findOne(@PathVariable("id") Long id) {
        log.info("GET /api/v1/employee/{}", id);
        return employeeService.findOne(id);
    }

    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        log.info("DELETE /api/v1/employee/{}", id);
        employeeService.delete(id);
    }
}
