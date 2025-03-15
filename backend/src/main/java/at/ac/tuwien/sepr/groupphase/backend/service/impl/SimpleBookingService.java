package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.enums.PaymentMethod;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.BookingService;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.BookingMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.PaymentService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.BookingValidator;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import at.ac.tuwien.sepr.groupphase.backend.entity.Pdf;
import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfGenerationService;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfStorageService;
import jakarta.mail.MessagingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class SimpleBookingService implements BookingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleBookingService.class);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final ApplicationUserRepository applicationUserRepository;
    private final BookingMapper bookingMapper;
    private final BookingValidator bookingValidator;
    private final UserService userService;
    private final SimpleMailService simpleMailService;
    private final SimplePdfGenerationService simplePdfGenerationService;
    private final PdfGenerationService pdfGenerationService;
    private final PdfStorageService pdfStorageService;
    private final PaymentService paymentService;


    public SimpleBookingService(
        BookingRepository bookingRepository,
        RoomRepository roomRepository,
        ApplicationUserRepository applicationUserRepository,
        BookingMapper bookingMapper,
        BookingValidator bookingValidator,
        UserService userService, SimpleMailService simpleMailService,
        SimplePdfGenerationService simplePdfGenerationService,
        PdfGenerationService pdfGenerationService,
        PdfStorageService pdfStorageService,
        PaymentService paymentService) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.applicationUserRepository = applicationUserRepository;
        this.bookingMapper = bookingMapper;
        this.bookingValidator = bookingValidator;
        this.userService = userService;
        this.simpleMailService = simpleMailService;
        this.simplePdfGenerationService = simplePdfGenerationService;
        this.pdfGenerationService = pdfGenerationService;
        this.pdfStorageService = pdfStorageService;
        this.paymentService = paymentService;
    }

    @Value("${application.booking.tax-id}")
    private String taxId;

    @Override
    public DetailedBookingDto createBooking(BookingCreateDto bookingCreateDto) throws ValidationException, MessagingException, IOException {
        LOGGER.debug("Create new booking: {}", bookingCreateDto);

        ApplicationUser loggedInUser = userService.getLoggedInUser();
        if (loggedInUser == null) {
            throw new AuthenticationCredentialsNotFoundException("User must be logged in to create a booking.");
        }

        bookingValidator.validateForCreate(bookingCreateDto, loggedInUser.getUsername());

        if (!isRoomAvailable(bookingCreateDto.roomId(), bookingCreateDto.startDate(), bookingCreateDto.endDate())) {
            throw new NotFoundException("Room with ID " + bookingCreateDto.roomId() + " is not available for the selected dates.");
        }

        Room room = roomRepository.findById(bookingCreateDto.roomId())
            .orElseThrow(() -> new NotFoundException("Room with ID " + bookingCreateDto.roomId() + " not found"));

        PaymentMethod paymentMethod = PaymentMethod.valueOf(bookingCreateDto.paymentMethod());
        LOGGER.info("Selected payment method: {}", paymentMethod);


        ApplicationUser user = userService.findApplicationUserByEmail(loggedInUser.getUsername());
        if (user == null) {
            throw new NotFoundException("User with email " + loggedInUser.getUsername() + " not found");
        }

        Booking booking = new Booking(room, loggedInUser, bookingCreateDto.startDate(), bookingCreateDto.endDate(), false, null);

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

        simpleMailService.sendEmail(savedBooking, List.of(pdf, pdf2));

        LOGGER.info("Booking confirmation email successfully sent to user: {}", savedBooking.getUser().getEmail());

        return bookingMapper.bookingToDetailedBookingDto(savedBooking);
    }

    @Override
    public List<DetailedBookingDto> findByUserId(String email) {
        LOGGER.debug("Find bookings for user with email {}", email);
        ApplicationUser user = userService.findApplicationUserByEmail(email);
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        return bookingMapper.bookingsToDetailedBookingDtos(bookings);
    }


    @Override
    public DetailedBookingDto findBookingById(Long bookingId) {
        LOGGER.debug("Find booking by ID {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking with ID " + bookingId + " not found"));
        return bookingMapper.bookingToDetailedBookingDto(booking);
    }

    public boolean isRoomAvailable(Long roomId, LocalDate startDate, LocalDate endDate) {
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

    @Transactional
    @Override
    public List<EmployeeBookingDto> getAllBookings() {
        LOGGER.info("Fetching all bookings.");
        LOGGER.debug("Find all bookings");
        List<Booking> bookings = bookingRepository.findAll();
        return bookingMapper.bookingsToEmployeeBookingDtos(bookings);
    }

    public Page<EmployeeBookingDto> getPagedBookings(Pageable pageable) {
        if (pageable.getPageNumber() < 0) {
            throw new IllegalArgumentException("Page index must not be less than zero.");
        }
        if (pageable.getPageSize() <= 0) {
            throw new IllegalArgumentException("Size must be greater than zero.");
        }
        Page<Booking> pagedBookings = bookingRepository.findAllWithCustomSorting(pageable);
        return pagedBookings.map(bookingMapper::bookingToEmployeeBookingDto);
    }

    @Transactional
    @Override
    public void cancelBooking(Long bookingId) throws NotFoundException, IOException, MessagingException, ConflictException {
        LOGGER.debug("Attempting to cancel booking with ID {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking is already cancelled", List.of());
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationDate(LocalDate.now());
        booking = bookingRepository.save(booking);
        if (booking.isPaid() && booking.getStripePaymentIntentId() != null) {
            paymentService.processRoomBookingRefund(booking);
        }

        byte[] cancellationPdf = simplePdfGenerationService.generateCancellation(booking);
        simpleMailService.sendCancellationEmail(booking, cancellationPdf);
        LOGGER.info("Cancellation email sent for booking ID {}", bookingId);
    }

    @Override
    public boolean updatePaymentStatus(Long bookingId) throws NotFoundException {

        Booking booking = bookingRepository.findBookingById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));
        try {
            Session session = Session.retrieve(booking.getStripeSessionId());
            PaymentIntent paymentIntent = PaymentIntent.retrieve(session.getPaymentIntent());
            LOGGER.debug("Payment for session {} with bookingId {} has status {}", session.getId(), bookingId, paymentIntent.getStatus());
            LOGGER.debug("PaymentIntent: {}", paymentIntent.getStatus());
            booking.setStripePaymentIntentId(paymentIntent.getId());
            booking.setPaid("succeeded".equals(paymentIntent.getStatus()));
        } catch (InvalidRequestException e) {
            LOGGER.error("Invalid request: {}", e.getMessage(), e);
        } catch (StripeException e) {
            LOGGER.error("Stripe exception: {}", e.getMessage(), e);
        }
        bookingRepository.save(booking);
        return booking.isPaid();
    }

    @Override
    public void markAsPaidManually(Long bookingId) throws NotFoundException, ConflictException {
        LOGGER.debug("Marking booking {} as paid manually", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new NotFoundException("Booking not found"));

        if (booking.isPaid()) {
            throw new ConflictException("Booking is already marked as paid.", null);
        }

        booking.setPaid(true);
        bookingRepository.save(booking);

        LOGGER.info("Booking {} marked as paid manually", bookingId);
    }


}
