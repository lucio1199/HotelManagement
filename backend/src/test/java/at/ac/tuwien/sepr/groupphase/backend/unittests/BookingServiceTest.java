package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.EmployeeBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.CheckInMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.RoomMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleBookingService;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.BookingMapper;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.BookingValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.lenient;

import at.ac.tuwien.sepr.groupphase.backend.service.PdfGenerationService;
import at.ac.tuwien.sepr.groupphase.backend.service.PdfStorageService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleMailService;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private BookingValidator bookingValidator;

    @InjectMocks
    private SimpleBookingService bookingService;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CheckInMapper checkInMapper;

    @Mock
    private UserService userService;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private PdfGenerationService pdfGenerationService;

    @Mock
    private PdfStorageService pdfStorageService;

    @Mock
    private SimpleMailService mailService;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(mailService, "javaMailSender", javaMailSender);
        ReflectionTestUtils.setField(mailService, "mailIntegrationId", "testemail@example.com");
    }

    /**
     * Test case to verify successful booking creation.
     */
    @Test
    public void givenValidBookingDto_whenCreateBooking_thenSaveAndReturn() throws ValidationException, ConflictException, MessagingException, IOException {

        BookingCreateDto bookingDto = new BookingCreateDto(1L, LocalDate.now(), LocalDate.now().plusDays(5), "PayCash");
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);
        user.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);


        DetailedBookingDto detailedBookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(5), "Room 1", 100.0, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13"), 2500.5, 2, "");

        when(userService.getLoggedInUser()).thenReturn(user);
        when(userService.findApplicationUserByEmail("test@example.com")).thenReturn(user);
        when(roomRepository.findById(bookingDto.roomId())).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking newBooking = invocation.getArgument(0);
            newBooking.setId(1L);
            return newBooking;
        });

        when(bookingMapper.bookingToDetailedBookingDto(any(Booking.class))).thenReturn(detailedBookingDto);

        byte[] mockPdfContent = "Mock PDF Content".getBytes();
        when(pdfGenerationService.generateBookingConfirmation(any(Booking.class))).thenReturn(mockPdfContent);
        lenient().doNothing().when(pdfStorageService).storePdf(eq(1L), eq("Buchungsbestätigung.pdf"), any(byte[].class));


            DetailedBookingDto result = bookingService.createBooking(bookingDto);

            assertNotNull(result);
            assertEquals(detailedBookingDto, result);
            verify(bookingRepository, times(1)).save(any(Booking.class));
            verify(mailService, times(1)).sendEmail(any(Booking.class), anyList());
            verify(pdfGenerationService, times(1)).generateBookingConfirmation(any(Booking.class));
            verify(pdfStorageService, times(1)).storePdf(eq(1L), eq("BookingConfirmation.pdf"), any(byte[].class));
    }


    /**
     * Test case to verify that attempting to create a booking with a non-existent room ID throws a {@link NotFoundException}.
     */
    @Test
    public void givenInvalidRoomId_whenCreateBooking_thenThrowNotFoundException() {
        BookingCreateDto bookingDto = new BookingCreateDto(1L, LocalDate.now(), LocalDate.now().plusDays(5), "PayCash");
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);

        when(userService.getLoggedInUser()).thenReturn(user);
        when(roomRepository.findById(bookingDto.roomId())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> bookingService.createBooking(bookingDto));
        assertEquals("Room with ID 1 not found", exception.getMessage());
    }

    /**
     * Test case to verify successful retrieval of bookings by user ID.
     */
    @Test
    public void givenValidUserId_whenFindBookingsByUserId_thenReturnBookings() {
        String email = "test@example.com";
        ApplicationUser user = new ApplicationUser();
        user.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);

        when(userService.findApplicationUserByEmail(email)).thenReturn(user);
        when(bookingRepository.findByUserId(user.getId())).thenReturn(List.of(booking));
        when(bookingMapper.bookingsToDetailedBookingDtos(List.of(booking))).thenReturn(List.of());

        List<DetailedBookingDto> result = bookingService.findByUserId(email);

        assertNotNull(result);
        verify(bookingRepository, times(1)).findByUserId(user.getId());
    }

    /**
     * Test case to verify that a booking, which is not paid, is successfully marked as paid manually.
     */
    @Test
    public void givenBookingNotPaid_whenMarkAsPaidManually_thenBookingIsMarkedAsPaid() throws NotFoundException, ConflictException {
        Long bookingId = 1L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setPaid(false);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingService.markAsPaidManually(bookingId);

        assertTrue(booking.isPaid());
        verify(bookingRepository, times(1)).save(booking);
    }

    /**
     * Test case to verify that when attempting to mark a non-existent booking as paid manually,
     */
    @Test
    public void givenNonExistentBooking_whenMarkAsPaidManually_thenThrowNotFoundException() {
        Long bookingId = 999L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> bookingService.markAsPaidManually(bookingId));
        assertEquals("Booking not found", exception.getMessage());
    }

    /**
     * Test case to verify that when attempting to update the payment status of a non-existent booking,
     */
    @Test
    public void givenNonExistentBooking_whenUpdatePaymentStatus_thenThrowNotFoundException() {
        Long bookingId = 999L;

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> bookingService.updatePaymentStatus(bookingId));
        assertEquals("Booking not found", exception.getMessage());
    }


    /**
     * Test case to verify that when there are no bookings in the repository,
     */
    @Test
    public void givenNoBookings_whenGetAllBookings_thenReturnEmptyList() {
        when(bookingRepository.findAll()).thenReturn(List.of());

        List<EmployeeBookingDto> result = bookingService.getAllBookings();

        assertTrue(result.isEmpty());
        verify(bookingRepository, times(1)).findAll();
    }

    /**
     * Test case to verify that attempting to cancel a booking that has already been cancelled throws a {@link ConflictException}.
     */
    @Test
    public void givenCancelledBooking_whenCancelBooking_thenThrowConflictException() throws NotFoundException, ConflictException {
        Long bookingId = 1L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.CANCELLED);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ConflictException exception = assertThrows(ConflictException.class,
            () -> bookingService.cancelBooking(bookingId));
        assertEquals("Booking is already cancelled. Conflicts: .", exception.getMessage());  // Angepasste Überprüfung
    }
}