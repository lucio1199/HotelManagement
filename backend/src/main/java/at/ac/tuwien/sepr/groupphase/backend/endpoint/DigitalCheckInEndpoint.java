package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInStatusDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckOutDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.GuestListDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.InviteToRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.BookingService;
import at.ac.tuwien.sepr.groupphase.backend.service.CheckInService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.CheckInValidator;
import jakarta.annotation.security.PermitAll;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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
import java.util.List;

@RestController
@RequestMapping("/api/v1/checkin")
public class DigitalCheckInEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final CheckInService checkInService;
    private final BookingService bookingService;
    private final CheckInValidator checkInValidator;
    private final UserService userService;

    public DigitalCheckInEndpoint(
        CheckInService checkInService,
        BookingService bookingService,
        UserService userService,
        CheckInValidator checkInValidator) {
        this.checkInService = checkInService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.checkInValidator = checkInValidator;

    }

    @Secured("ROLE_GUEST")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void checkIn(
        @Valid @ModelAttribute CheckInDto checkInDto,
        @RequestPart(value = "passport", required = true) MultipartFile passport) throws ValidationException, ConflictException, IOException {
        LOGGER.info("POST /api/v1/checkin");
        LOGGER.debug("request body: {}", checkInDto);
        if (passport != null) {
            LOGGER.debug("passport_type: {}, passport_name: {}, passport_size: {}", passport.getContentType(), passport.getOriginalFilename(), passport.getSize());
        }
        checkInService.checkIn(checkInDto, passport, userService.getLoggedInUserEmail());
    }

    @PermitAll
    @GetMapping("/rooms")
    @ResponseStatus(HttpStatus.OK)
    public DetailedRoomDto[] getGuestRoom() {
        LOGGER.info("GET /api/v1/checkin/room");
        return checkInService.getGuestRooms(userService.getLoggedInUserEmail());
    }

    @Secured("ROLE_GUEST")
    @GetMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public DetailedBookingDto getBookingById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/checkin/{}", id);
        return checkInService.findBookingById(id, userService.getLoggedInUser().getId());
    }

    @Secured("ROLE_GUEST")
    @PostMapping("/checkout")
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public void checkOut(@RequestBody CheckOutDto checkOutDto) throws NotFoundException, ValidationException {
        LOGGER.info("POST /api/v1/checkin/checkout");
        LOGGER.debug("request payload: {}", checkOutDto);
        CheckOutDto newDto = new CheckOutDto(checkOutDto.bookingId(), userService.getLoggedInUserEmail());
        checkInService.checkOut(newDto);
    }

    @Secured("ROLE_GUEST")
    @GetMapping("/booking/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public DetailedBookingDto getGuestBooking(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/checkin/booking");
        return checkInService.getGuestBooking(id, userService.getLoggedInUserEmail());
    }

    @GetMapping("/booking-ids")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> getAllBookingIds() {
        LOGGER.info("GET /api/v1/checkin/bookingIds");
        return checkInService.getAllBookingIds();
    }

    @PermitAll
    @GetMapping("/checkin-status")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public CheckInStatusDto[] getCheckedInStatus() {
        LOGGER.info("GET /api/v1/checkin/checkin-status");
        return checkInService.getCheckedInStatus(userService.getLoggedInUserEmail());
    }

    @Secured("ROLE_GUEST")
    @PostMapping("/to-room")
    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    public void inviteToRoom(@RequestBody InviteToRoomDto inviteToRoomDto) throws NotFoundException, ValidationException, ConflictException, MessagingException {
        LOGGER.info("POST /api/v1/checkin/to-room");
        LOGGER.debug("request body: {}", inviteToRoomDto);
        checkInService.inviteToRoom(inviteToRoomDto, userService.getLoggedInUserEmail());
    }

    @Secured("ROLE_GUEST")
    @GetMapping("/guests/{id}")
    @ResponseStatus(HttpStatus.OK)
    public GuestListDto[] getGuests(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/checkin/guests/{}", id);
        return checkInService.getGuests(id, userService.getLoggedInUserEmail());
    }

    @Secured("ROLE_GUEST")
    @GetMapping("/owner/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Boolean> getOwner(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/checkin/owner/{}", id);
        boolean isOwner = checkInService.isOwner(id, userService.getLoggedInUserEmail());
        return ResponseEntity.ok(isOwner);
    }
}
