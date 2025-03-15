package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.AddToRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckOutDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.OccupancyDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.BookingService;
import at.ac.tuwien.sepr.groupphase.backend.service.CheckInService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.CheckInValidator;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

@RestController
@RequestMapping("/api/v1/manual-checkin")
public class ManualCheckInEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CheckInService checkInService;
    private final BookingService bookingService;
    private final CheckInValidator checkInValidator;
    private final UserService userService;

    public ManualCheckInEndpoint(
        CheckInService checkInService,
        BookingService bookingService,
        UserService userService,
        CheckInValidator checkInValidator) {
        this.checkInService = checkInService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.checkInValidator = checkInValidator;

    }

    @Secured({"ROLE_ADMIN", "ROLE_RECEPTIONIST"})
    @PostMapping(value = "/{email}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void checkIn(
        @PathVariable("email") String email,
        @Valid @ModelAttribute CheckInDto checkInDto,
        @RequestPart(value = "passport", required = true) MultipartFile passport) throws ValidationException, ConflictException, IOException {
        LOGGER.info("POST /api/v1/manual-checkin");
        LOGGER.debug("request email: {}, request body: {}", email, checkInDto);
        if (passport != null) {
            LOGGER.debug("passport_type: {}, passport_name: {}, passport_size: {}", passport.getContentType(), passport.getOriginalFilename(), passport.getSize());
        }
        checkInService.checkIn(checkInDto, passport, email);
    }

    @Secured({"ROLE_ADMIN", "ROLE_RECEPTIONIST"})
    @GetMapping("/{id}/{email}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public DetailedBookingDto getBookingById(@PathVariable("id") Long id, @PathVariable("email") String email) {
        LOGGER.info("GET /api/v1/manual-checkin/{}/{}", id, email);
        return checkInService.findBookingById(id, userService.findApplicationUserByEmail(email).getId());
    }

    @Secured({"ROLE_ADMIN", "ROLE_RECEPTIONIST"})
    @GetMapping("/checkin-status/{email}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public CheckInStatusDto[] getCheckedInStatus(@PathVariable("email") String email) {
        LOGGER.info("GET /api/v1/manual-checkin/checkin-status");
        return checkInService.getCheckedInStatus(email);
    }

    @Secured({"ROLE_ADMIN", "ROLE_RECEPTIONIST"})
    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    public void checkOut(@RequestBody CheckOutDto checkOutDto) throws NotFoundException, ValidationException {
        LOGGER.info("POST /api/v1/manual-checkin/checkout");
        LOGGER.debug("request payload: {}", checkOutDto);
        checkInService.checkOut(checkOutDto);
    }

    @Secured({"ROLE_ADMIN", "ROLE_RECEPTIONIST", "ROLE_CLEANING_STAFF"})
    @GetMapping("/occupancy/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public OccupancyDto getOccupancyStatus(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/manual-checkin/occupancy/{}", id);
        return checkInService.getOccupancyStatus(id);
    }

    @Secured({"ROLE_ADMIN", "ROLE_RECEPTIONIST", "ROLE_CLEANING_STAFF"})
    @PostMapping(path = "/to-room", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void addToRoom(
        @Valid @ModelAttribute AddToRoomDto addToRoomDto,
        @RequestPart(value = "passport", required = true) MultipartFile passport) throws ValidationException, ConflictException, IOException {
        LOGGER.info("POST /api/v1/manual-checkin/toRoom");
        LOGGER.debug("request body: {}", addToRoomDto);
        if (passport != null) {
            LOGGER.debug("passport_type: {}, passport_name: {}, passport_size: {}", passport.getContentType(), passport.getOriginalFilename(), passport.getSize());
        }
        checkInService.addToRoom(addToRoomDto, passport, userService.getLoggedInUserEmail());
    }

    @Secured({"ROLE_ADMIN", "ROLE_RECEPTIONIST"})
    @GetMapping("/all-guests/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GuestListDto[] getAllGuests(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/manual-checkin/guests/{}", id);
        return checkInService.getAllGuests(id);
    }

    @Secured({"ROLE_ADMIN", "ROLE_RECEPTIONIST"})
    @DeleteMapping("/{bookingId}/{email}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public void remove(@PathVariable("bookingId") Long bookingId, @PathVariable("email") String email) {
        LOGGER.info("DELETE /api/v1/manual-checkin/{}/{}", bookingId, email);
        checkInService.remove(bookingId, email);
    }
}
