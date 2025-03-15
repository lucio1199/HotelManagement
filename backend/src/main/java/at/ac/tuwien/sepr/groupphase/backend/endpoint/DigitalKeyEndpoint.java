package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.KeyStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.service.KeyService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping("/api/v1/key")
public class DigitalKeyEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final KeyService keyService;
    private final UserService userService;

    public DigitalKeyEndpoint(
        KeyService keyService,
        UserService userService) {
        this.keyService = keyService;
        this.userService = userService;
    }

    @Secured("ROLE_GUEST")
    @PostMapping("/unlock/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void unlock(@PathVariable("id") Long id) throws NotFoundException, IOException {
        LOGGER.info("POST /api/v1/key/unlock/{}", id);
        keyService.unlock(id, userService.getLoggedInUserEmail());
    }

    @Secured("ROLE_GUEST")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public KeyStatusDto getStatus(@PathVariable("id") Long id) throws NotFoundException, IOException {
        LOGGER.info("GET /api/v1/key/{}", id);
        return keyService.getStatus(id, userService.getLoggedInUserEmail());
    }
}
