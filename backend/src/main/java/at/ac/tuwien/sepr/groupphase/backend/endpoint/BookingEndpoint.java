package at.ac.tuwien.sepr.groupphase.backend.endpoint;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.service.BookingService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;

import com.stripe.exception.StripeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.ResponseEntity;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.PageRequest;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfGenerationService;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfStorageService;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;
import org.springframework.http.ContentDisposition;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingEndpoint.class);
    private final BookingService bookingService;
    private final UserService userService;
    private final PdfStorageService pdfStorageService;

    public BookingEndpoint(BookingService bookingService, UserService userService, PdfStorageService pdfStorageService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.pdfStorageService = pdfStorageService;
    }

    @Secured("ROLE_GUEST")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DetailedBookingDto createBooking(@RequestBody BookingCreateDto bookingCreateDto) throws ValidationException, ConflictException, MessagingException, IOException {
        LOGGER.info("POST /api/v1/bookings body: {}", bookingCreateDto);
        return bookingService.createBooking(bookingCreateDto);
    }

    @Secured("ROLE_GUEST")
    @GetMapping("/my-bookings")
    @ResponseStatus(HttpStatus.OK)
    public List<DetailedBookingDto> getBookingsByUser() {
        LOGGER.info("GET /api/v1/bookings/my-bookings for current logged-in user");
        String loggedInUserEmail = userService.getLoggedInUserEmail();
        return bookingService.findByUserId(loggedInUserEmail);
    }

    @Secured("ROLE_GUEST")
    @GetMapping("/{id}")
    @Transactional
    @ResponseStatus(HttpStatus.OK)
    public DetailedBookingDto getBookingById(@PathVariable("id") Long id) {
        LOGGER.info("GET /api/v1/bookings/{}", id);
        return bookingService.findBookingById(id);
    }

    @Secured({"ROLE_ADMIN", "ROLE_RECEPTIONIST", "ROLE_CLEANING_STAFF"})
    @GetMapping("/managerbookings")
    @ResponseStatus(HttpStatus.OK)
    public List<EmployeeBookingDto> getAllBookings() {
        LOGGER.info("GET /api/v1/bookings/managerbookings");
        return bookingService.getAllBookings();
    }

    @Secured({"ROLE_ADMIN", "ROLE_RECEPTIONIST", "ROLE_CLEANING_STAFF"})
    @GetMapping("/managerbookings/paged")
    @ResponseStatus(HttpStatus.OK)
    public Page<EmployeeBookingDto> getPagedBookings(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "50") int size
    ) {
        LOGGER.info("GET /api/v1/bookings/managerbookings/paged?page={}&size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        return bookingService.getPagedBookings(pageable);
    }


    @Secured("ROLE_GUEST")
    @GetMapping("/my-bookings/{bookingId}/pdf/{type}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long bookingId, @PathVariable String type) {
        LOGGER.info("GET /api/v1/bookings/my-bookings/{}/pdf/{}", bookingId, type);

        Pdf pdf = pdfStorageService.getPdf(bookingId, type);
        byte[] content = pdf.getContent();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
            .filename(type)
            .build());

        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }

    @Secured("ROLE_GUEST")
    @DeleteMapping("/my-bookings/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable("bookingId") Long bookingId) throws MessagingException, IOException, ConflictException {
        LOGGER.info("DELETE /api/v1/bookings/my-bookings/{}/cancel", bookingId);
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_GUEST")
    @PutMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public Boolean markAsPaid(@PathVariable("bookingId") Long bookingId) throws StripeException {
        LOGGER.info("PUT /api/v1/bookings/paymentSuccessful/{}", bookingId);
        return bookingService.updatePaymentStatus(bookingId);
    }

    @Secured({"ROLE_ADMIN"})
    @PutMapping("/{bookingId}/mark-as-paid")
    public ResponseEntity<?> markBookingAsPaid(@PathVariable("bookingId") Long bookingId) throws ConflictException, NotFoundException {
        LOGGER.info("PUT /api/v1/bookings/{}/mark-as-paid", bookingId);
        bookingService.markAsPaidManually(bookingId);
        return ResponseEntity.ok().build();
    }

}
