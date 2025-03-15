package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.ActivityBookingService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/activity-booking")
public class ActivityBookingEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityBookingEndpoint.class);
    private final ActivityBookingService activityBookingService;
    private final UserService userService;

    public ActivityBookingEndpoint(ActivityBookingService activityBookingService, UserService userService) {
        this.activityBookingService = activityBookingService;
        this.userService = userService;
    }

    @Secured("ROLE_GUEST")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityBookingDto createBooking(@RequestBody ActivityBookingCreateDto bookingCreateDto) throws NotFoundException, ValidationException {
        LOGGER.info("POST /api/v1/bookings body: {}", bookingCreateDto);
        String loggedInUserEmail = userService.getLoggedInUserEmail();
        return activityBookingService.createBooking(bookingCreateDto, loggedInUserEmail);
    }

    @Secured("ROLE_GUEST")
    @PutMapping("/{bookingId}")
    public ResponseEntity<?> markAsPaid(@PathVariable("bookingId") Long bookingId) {
        LOGGER.info("PUT /api/v1/bookings/paymentSuccessful/{}", bookingId);
        activityBookingService.updatePaymentStatus(bookingId);
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_GUEST")
    @GetMapping("/my-bookings")
    @ResponseStatus(HttpStatus.OK)
    public List<ActivityBookingDto> getBookingsByUser() {
        LOGGER.info("GET /api/v1/bookings/my-bookings for current logged-in user");
        String loggedInUserEmail = userService.getLoggedInUserEmail();
        return activityBookingService.findByUserEmail(loggedInUserEmail);
    }
}
