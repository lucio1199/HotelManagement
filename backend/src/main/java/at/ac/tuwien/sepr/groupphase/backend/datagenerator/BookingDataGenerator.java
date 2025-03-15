package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.BookingMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
import at.ac.tuwien.sepr.groupphase.backend.enums.PaymentMethod;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfGenerationService;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfStorageService;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

@Component
public class BookingDataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final int NUMBER_OF_PAST_BOOKINGS_TO_GENERATE = 600;
    private static final int NUMBER_OF_BOOKINGS_TO_GENERATE = 10;
    private static final int NUMBER_OF_FUTURE_BOOKINGS_TO_GENERATE = 100;
    private final Random random = new Random();

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final PdfGenerationService pdfGenerationService;
    private final PdfStorageService pdfStorageService;
    private final BookingMapper bookingMapper;
    private final GuestRepository guestRepository;

    private List<Guest> users;

    public BookingDataGenerator(BookingRepository bookingRepository, RoomRepository roomRepository,
                                ApplicationUserRepository applicationUserRepository,
                                PdfGenerationService pdfGenerationService,
                                PdfStorageService pdfStorageService, BookingMapper bookingMapper,
                                GuestRepository guestRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.pdfGenerationService = pdfGenerationService;
        this.pdfStorageService = pdfStorageService;
        this.bookingMapper = bookingMapper;
        this.guestRepository = guestRepository;
    }

    /**
     * Generates NUMBER_OF_BOOKINGS_TO_GENERATE present bookings.
     */
    @Transactional
    public void generatePresentBookings() {
        LOGGER.debug("generating {} present booking entries", NUMBER_OF_BOOKINGS_TO_GENERATE);

        Random random = new Random();
        for (int i = 1; i <= NUMBER_OF_BOOKINGS_TO_GENERATE - 2; i++) {
            try {
                ApplicationUser user = applicationUserRepository.findByEmail("guest" + i + "@example.com").orElseThrow(() ->
                    new NotFoundException("User was not found."));
                List<Room> rooms = roomRepository.findAll();
                Room room = rooms.get(i - 1);
                Booking booking = Booking.BookingBuilder.aBooking()
                    .withRoom(room)
                    .withUser(user)
                    .withStartDate(LocalDate.now())
                    .withEndDate(LocalDate.now().plusDays(1 + random.nextInt(21)))
                    .build();
                LOGGER.debug("saving booking for {}", booking.getUser().getEmail());
                bookingRepository.save(booking);
                LOGGER.debug("Saved booking for {}", booking.getUser().getEmail());
                LOGGER.info("Email send for booking ID {}", booking.getId());
                savePdfForBooking(booking);
            } catch (NoSuchElementException | IndexOutOfBoundsException ex) {
                LOGGER.debug("Could not create more bookings:" + ex.getMessage());
            }
        }
        ApplicationUser user = applicationUserRepository.findByEmail("guest" + 10 + "@example.com").orElseThrow(() ->
            new NotFoundException("User was not found."));
        List<Room> rooms = roomRepository.findAll();
        Room room = rooms.get(10 - 1);
        Booking booking = Booking.BookingBuilder.aBooking()
            .withRoom(room)
            .withUser(user)
            .withStartDate(LocalDate.now())
            .withEndDate(LocalDate.now().plusDays(random.nextInt(21)))
            .build();
        LOGGER.debug("saving booking for {}", booking.getUser().getEmail());
        bookingRepository.save(booking);
        savePdfForBooking(booking);
        room = rooms.get(10 - 2);
        booking = Booking.BookingBuilder.aBooking()
            .withRoom(room)
            .withUser(user)
            .withStartDate(LocalDate.now())
            .withEndDate(LocalDate.now().plusDays(random.nextInt(21)))
            .build();
        LOGGER.debug("saving booking for {}", booking.getUser().getEmail());
        bookingRepository.save(booking);
        savePdfForBooking(booking);
    }

    /**
     * Generates NUMBER_OF_PAST_BOOKINGS_TO_GENERATE past bookings.
     */
    @Transactional
    public void generatePastBookings() {
        this.users = guestRepository.findAll();
        LOGGER.debug("Added {} users.", this.users.size());
        LOGGER.debug("generating {} past booking entries", NUMBER_OF_PAST_BOOKINGS_TO_GENERATE);

        Random random = new Random();
        for (int i = 1; i <= NUMBER_OF_PAST_BOOKINGS_TO_GENERATE - 2; i++) {
            boolean bookingCreated = false;
            while (!bookingCreated) { // Retry loop
                try {
                    List<Room> rooms = roomRepository.findAll();
                    Room room = rooms.get(random.nextInt(rooms.size())); // Randomly pick a room
                    LocalDate startDate = LocalDate.now().minusDays(30 + random.nextInt(1056));
                    String paymentMethod = random.nextInt(3) % 2 == 0 ? "PayCash" : "PayInAdvance";
                    BookingCreateDto bookingCreateDto = new BookingCreateDto(room.getId(), startDate, startDate.plusDays(1 + random.nextInt(7)), paymentMethod);
                    LOGGER.debug("Attempting to save booking: {} ", bookingCreateDto);

                    // Try to create the booking
                    DetailedBookingDto result = createBooking(bookingCreateDto, getRandomUser());

                    // If booking is created successfully, exit the retry loop
                    LOGGER.debug("Successfully saved booking: {}", result);
                    bookingCreated = true;
                } catch (IllegalArgumentException | NotFoundException | NoSuchElementException | IndexOutOfBoundsException | ValidationException | MessagingException | IOException ex) {
                    // Log the error and continue to retry
                    LOGGER.debug("Error while creating booking (retrying): {}", ex.getMessage());
                }
            }
        }
    }

    /**
     * Generates NUMBER_OF_FUTURE_BOOKINGS_TO_GENERATE future bookings.
     */
    @Transactional
    public void generateFutureBookings() {
        this.users = guestRepository.findAll();
        LOGGER.debug("Added {} users.", this.users.size());
        LOGGER.debug("generating {} future booking entries", NUMBER_OF_FUTURE_BOOKINGS_TO_GENERATE);

        Random random = new Random();
        for (int i = 1; i <= NUMBER_OF_FUTURE_BOOKINGS_TO_GENERATE - 2; i++) {
            boolean bookingCreated = false;
            while (!bookingCreated) { // Retry loop
                try {
                    List<Room> rooms = roomRepository.findAll();
                    Room room = rooms.get(random.nextInt(rooms.size())); // Randomly pick a room
                    LocalDate startDate = LocalDate.now().plusDays(30 + random.nextInt(326));
                    String paymentMethod = random.nextInt(3) % 2 == 0 ? "PayCash" : "PayInAdvance";
                    BookingCreateDto bookingCreateDto = new BookingCreateDto(room.getId(), startDate, startDate.plusDays(1 + random.nextInt(14)), paymentMethod);
                    LOGGER.debug("Attempting to save booking: {} ", bookingCreateDto);

                    // Try to create the booking
                    DetailedBookingDto result = createBooking(bookingCreateDto, getRandomUser());

                    // If booking is created successfully, exit the retry loop
                    LOGGER.debug("Successfully saved booking: {}", result);
                    bookingCreated = true;
                } catch (IllegalArgumentException | NotFoundException | NoSuchElementException | IndexOutOfBoundsException | ValidationException | MessagingException | IOException ex) {
                    // Log the error and continue to retry
                    LOGGER.debug("Error while creating booking (retrying): {}", ex.getMessage());
                }
            }
        }
    }

    /**
     * Saves Booking Confirmation, Invoice for a Booking.
     *
     * @param booking The booking of the corresponding PDFs.
     */
    private void savePdfForBooking(Booking booking) {
        try {
            byte[] bookingPdf = pdfGenerationService.generateBookingConfirmation(booking);
            pdfStorageService.storePdf(booking.getId(), "BookingConfirmation.pdf", bookingPdf);
            byte[] invoicePdf = pdfGenerationService.generateInvoice(booking);
            pdfStorageService.storePdf(booking.getId(), "Invoice.pdf", invoicePdf);
            LOGGER.info("Stored PDFs for booking ID {} in database", booking.getId());
        } catch (Exception e) {
            LOGGER.error("Error storing PDFs for booking ID {}: {}", booking.getId(), e.getMessage());
        }
    }

    /**
     * Clears all existing bookings from the database.
     */
    public void clearExistingBookings() {
        List<Booking> existingBookings = bookingRepository.findAll();
        if (!existingBookings.isEmpty()) {
            LOGGER.debug("clearing {} existing booking entries", existingBookings.size());
            for (Booking booking : existingBookings) {
                try {
                    pdfStorageService.deletePdf(booking.getId(), "BookingConfirmation.pdf");
                    pdfStorageService.deletePdf(booking.getId(), "Invoice.pdf");
                } catch (NotFoundException e) {
                    LOGGER.warn("No PDF found for booking ID {}", booking.getId());
                }
            }
            bookingRepository.deleteAll();
        }
    }

    /**
     * Creates a new booking.
     *
     * @param bookingCreateDto Details of the booking (room ID, user ID, dates).
     * @return Detailed information about the created booking.
     * @throws NotFoundException If the room is not found.
     * @throws ValidationException If the room is not available or data is invalid.
     * @throws MessagingException If there is an error with sending emails.
     * @throws IOException If there is an error generating PDF or other resources.
     */
    private DetailedBookingDto createBooking(BookingCreateDto bookingCreateDto, ApplicationUser user) throws NotFoundException, ValidationException, MessagingException, IOException {
        validateForCreate(bookingCreateDto, user.getUsername());

        if (!isRoomAvailable(bookingCreateDto.roomId(), bookingCreateDto.startDate(), bookingCreateDto.endDate())) {
            throw new NotFoundException("Room with ID " + bookingCreateDto.roomId() + " is not available for the selected dates.");
        }

        Room room = roomRepository.findById(bookingCreateDto.roomId())
            .orElseThrow(() -> new NotFoundException("Room with ID " + bookingCreateDto.roomId() + " not found"));

        PaymentMethod paymentMethod = PaymentMethod.valueOf(bookingCreateDto.paymentMethod());
        LOGGER.info("Selected payment method: {}", paymentMethod);

        Booking booking = new Booking(room, user, bookingCreateDto.startDate(), bookingCreateDto.endDate(), false, null);

        if (paymentMethod.equals(PaymentMethod.PayInAdvance)) {
            booking.setStripeSessionId("SessionID");
            booking.setPaid(true);
        }


        if (bookingCreateDto.endDate().isBefore(LocalDate.now())) {
            if (random.nextInt(100) == 42) {  // randomly let 1% of bookings be unpaid
                booking.setPaid(false);
            } else {
                booking.setPaid(true);
            }
        }
        Booking savedBooking = bookingRepository.save(booking);

        LOGGER.info("Booking saved with ID {}", savedBooking.getId());

        byte[] pdfContent = pdfGenerationService.generateBookingConfirmation(savedBooking);
        Pdf pdf = new Pdf();
        pdf.setContent(pdfContent);
        pdf.setDocumentType("BookingConfirmation.pdf");

        pdfStorageService.storePdf(savedBooking.getId(), pdf.getDocumentType(), pdfContent);

        byte[] pdfContent2 = pdfGenerationService.generateInvoice(savedBooking);
        final Pdf pdf2 = new Pdf();
        pdf2.setContent(pdfContent2);
        pdf2.setDocumentType("Invoice.pdf");
        pdfStorageService.storePdf(savedBooking.getId(), pdf2.getDocumentType(), pdfContent2);

        LOGGER.info("Booking confirmation email successfully sent to user: {}", savedBooking.getUser().getEmail());

        return bookingMapper.bookingToDetailedBookingDto(savedBooking);
    }

    /**
     * Checks if a room is available for booking.
     *
     * @param roomId The id of the room.
     * @param startDate The start date of the booking.
     * @param endDate The end date of the booking.
     * @return If the room is available.
     */
    private boolean isRoomAvailable(Long roomId, LocalDate startDate, LocalDate endDate) {
        LOGGER.debug("Check if room with ID {} is available between {} and {}", roomId, startDate, endDate);

        boolean isAvailable = bookingRepository.countByRoomIdAndDateRangeAndStatus(
            roomId,
            startDate,
            endDate,
            List.of(BookingStatus.PENDING, BookingStatus.ACTIVE, BookingStatus.COMPLETED)
        ) == 0;

        LOGGER.debug("Room availability for ID {}: {}", roomId, isAvailable);
        return isAvailable;
    }

    /**
     * Gets a random ApplicationUser.
     *
     * @return The ApplicationUser.
     */
    private ApplicationUser getRandomUser() {
        return users.get(random.nextInt(users.size()));
    }

    /**
     * Validates the {@link BookingCreateDto} before creating a new booking.
     * Ensures that all required fields are valid, including dates and user information.
     *
     * @param bookingCreateDto the {@link BookingCreateDto} to be validated.
     * @param loggedInEmail the email of the logged-in user who is making the booking.
     * @throws ValidationException if the validation fails, with a list of errors.
     */
    private void validateForCreate(BookingCreateDto bookingCreateDto, String loggedInEmail) throws ValidationException {
        LOGGER.trace("validateForCreate({})", bookingCreateDto);
        List<String> validationErrors = new ArrayList<>();

        validateRoomId(bookingCreateDto.roomId(), validationErrors);
        validateEmail(loggedInEmail, validationErrors);
        validateDates(bookingCreateDto.startDate(), bookingCreateDto.endDate(), validationErrors);
        validatePaymentMethod(bookingCreateDto.paymentMethod(), validationErrors);

        if (!validationErrors.isEmpty()) {
            LOGGER.debug("Validation errors: {}", validationErrors);
            throw new ValidationException("Validation failed for one or more fields.", validationErrors);
        }
    }

    /**
     * Validates the room ID.
     * Ensures that the room ID is not null and is a positive number.
     *
     * @param roomId the room ID to validate.
     * @param errors the list of validation errors.
     */
    private void validateRoomId(Long roomId, List<String> errors) {
        if (roomId == null) {
            errors.add("Room ID must not be null.");
        } else if (roomId <= 0) {
            errors.add("Room ID must be a positive number.");
        }
    }

    /**
     * Validates the email format.
     * Ensures that the email is not null, is not blank, and has a valid format.
     *
     * @param email the email to validate.
     * @param errors the list of validation errors.
     */
    private void validateEmail(String email, List<String> errors) {
        if (email == null || email.isBlank()) {
            errors.add("Email must not be null or blank.");
        } else if (!email.matches("^[\\w-_.+]*[\\w-_.]@[\\w]+[.][a-z]+$")) {
            errors.add("Email format is invalid.");
        } else if (email.length() > 255) {
            errors.add("Email must not exceed 255 characters.");
        }
    }


    /**
     * Validates the start and end dates for the booking.
     * Ensures that the dates are valid and that the start date is before or equal to the end date.
     *
     * @param startDate the start date of the booking.
     * @param endDate the end date of the booking.
     * @param errors the list of validation errors.
     */
    private void validateDates(LocalDate startDate, LocalDate endDate, List<String> errors) {
        if (startDate == null) {
            errors.add("Start date must not be null.");
        }
        if (endDate == null) {
            errors.add("End date must not be null.");
        }

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                errors.add("Start date must be before or equal to the end date.");
            }
        }

        if (startDate != null && startDate.isEqual(endDate)) {
            errors.add("Start date and end date must be at least 1 day apart.");
        }
    }

    /**
     * Validates the payment method for the booking.
     * Ensures that the provided payment method is a valid value from the {@link PaymentMethod} enum.
     *
     * @param paymentMethod the payment method to validate.
     * @param errors the list of validation errors.
     */
    private void validatePaymentMethod(String paymentMethod, List<String> errors) {
        try {
            PaymentMethod.valueOf(paymentMethod);
        } catch (IllegalArgumentException | NullPointerException e) {
            errors.add("Invalid payment method: " + paymentMethod);
        }
    }
}
