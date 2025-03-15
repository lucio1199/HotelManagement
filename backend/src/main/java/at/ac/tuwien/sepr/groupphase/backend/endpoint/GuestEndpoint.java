package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestCreateUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestDetailDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestSearchDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestSignupDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.SimpleGuestDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.GuestService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;


/**
 * REST endpoint for guest related actions.
 */
@RestController
@Slf4j
@RequestMapping("api/v1/guest")
public class GuestEndpoint {
    private final GuestService guestService;

    public GuestEndpoint(GuestService guestService) {
        this.guestService = guestService;
    }

    /**
     * Sign up a new guest.
     *
     * @param guestSignupDto the guest to sign up
     * @return the signed up guest
     * @throws ValidationException if the guest is not valid
     * @throws ConflictException   if the guest already exists
     */
    @PostMapping("/signup")
    @PermitAll
    @ResponseStatus(HttpStatus.CREATED)
    public SimpleGuestDto signup(
        @Valid @RequestBody GuestSignupDto guestSignupDto) throws ValidationException, ConflictException {
        log.info("POST /api/v1/guest/signup/{},", guestSignupDto);
        return guestService.signup(guestSignupDto);
    }

    /**
     * Find all guests.
     *
     * @return all guests
     */
    @Secured("ROLE_ADMIN")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<GuestListDto> findAll(Pageable pageable) {
        log.info("GET /api/v1/employee");
        Page<GuestListDto> all = guestService.findAll(pageable);
        return all;
    }

    /**
     * Find a guest by email.
     *
     * @param email the email of the guest
     * @return the guest
     * @throws ValidationException if the email is not valid
     */
    @Secured({"ROLE_ADMIN", "ROLE_GUEST"})
    @GetMapping(value = "/{email}")
    @ResponseStatus(HttpStatus.OK)
    public GuestDetailDto findByEmail(
        @PathVariable("email") String email) throws ValidationException, NotFoundException {
        log.info("GET /api/v1/guest/{}", email);
        return guestService.findByEmail(email);
    }

    /**
     * Update a guest.
     *
     * @param email    the email of the guest
     * @param guestDto the guest to update
     * @return the updated guest
     * @throws ValidationException if the guest is not valid
     * @throws NotFoundException   if the guest does not exist
     */
    @Secured("ROLE_ADMIN")
    @PutMapping(value = "/{email}")
    @ResponseStatus(HttpStatus.OK)
    public GuestDetailDto update(
        @PathVariable("email") String email,
        @Valid @RequestBody GuestCreateUpdateDto guestDto) throws ValidationException, NotFoundException, IllegalArgumentException {
        log.info("PUT /api/v1/guest/{}", guestDto);
        return guestService.update(email, guestDto);
    }

    /**
     * Create a new guest.
     *
     * @param guestDto the guest to create
     * @return the created guest
     * @throws ValidationException if the guest is not valid
     */
    @Secured("ROLE_ADMIN")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuestDetailDto create(
        @Valid @RequestBody GuestCreateUpdateDto guestDto) throws ValidationException, ConflictException {
        log.info("POST /api/v1/guest/{}", guestDto);
        return guestService.create(guestDto);
    }

    /**
     * Delete a guest by email.
     *
     * @param email the email of the guest
     * @throws ValidationException if the email is not valid
     */
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuest(@PathVariable("email") String email) throws ValidationException {
        log.info("DELETE /api/v1/guests/{}", email);
        guestService.deleteByEmail(email);
    }

    /**
     * Search for guests.
     *
     * @param guestSearchDto the search criteria
     * @return the found guests
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public Page<GuestListDto> search(GuestSearchDto guestSearchDto, Pageable pageable) {
        log.info("GET /api/v1/guest with criteria:{}", guestSearchDto);
        return guestService.search(guestSearchDto, pageable);
    }

}