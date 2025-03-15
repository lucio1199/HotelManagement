package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.GuestMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleGuestService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.GuestValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuestServiceTest {

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private GuestMapper guestMapper;

    @Mock
    private GuestValidator guestValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SimpleGuestService guestService;

    private Guest guest;


    @BeforeEach
    void setup() {

        guest = new Guest();
        guest.setId(1L);
        guest.setEmail("test@example.com");
        guest.setFirstName("John");
        guest.setLastName("Doe");
        guest.setPassword("Password@123");
        guest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        guest.setGender(Gender.MALE);
    }

    @Test
    void testSignup_withValidData_shouldCreateGuest() throws ValidationException, ConflictException {
        GuestSignupDto guestSignupDto = new GuestSignupDto("test@example.com", "Password@123");
        guest.setEmail(guestSignupDto.email());
        Guest savedGuest = new Guest();
        savedGuest.setEmail(guestSignupDto.email());
        savedGuest.setPassword("EncodedPassword");
        SimpleGuestDto simpleGuestDto = new SimpleGuestDto("test@example.com");

        when(guestRepository.existsByEmail(guestSignupDto.email())).thenReturn(false);
        when(guestMapper.guestSignupDtoToGuest(any(GuestSignupDto.class))).thenReturn(guest);
        when(passwordEncoder.encode(guestSignupDto.password())).thenReturn("EncodedPassword");
        when(guestRepository.save(guest)).thenReturn(savedGuest);
        when(guestMapper.guestToSimpleGuestDto(savedGuest)).thenReturn(simpleGuestDto);

        SimpleGuestDto result = guestService.signup(guestSignupDto);

        assertAll(
            () -> assertNotNull(result, "Result should not be null"),
            () -> assertEquals("test@example.com", result.email(), "Email should match the input email"),
            () -> verify(guestValidator).validateForSignup(guestSignupDto),
            () -> verify(guestRepository).save(guest)
        );
    }


    @Test
    @Disabled
    void testFindAll_shouldReturnListOfGuests() {
        when(guestRepository.findAll()).thenReturn(List.of(guest));
        when(guestMapper.guestToGuestListDto(guest)).thenReturn(new GuestListDto("John", "Doe", "test@example.com"));

        Page<GuestListDto> result = guestService.findAll(PageRequest.of(0, 10));

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals(1, result.getSize()),
            () -> verify(guestRepository).findAll()
        );
    }

    @Test
    void testUpdate_withValidData_shouldUpdateGuest() throws ValidationException, NotFoundException {
        GuestCreateUpdateDto updateDto = new GuestCreateUpdateDto("UpdatedJohn", "UpdatedDoe", null, null, null, null, null, null, null, null, "NewPassword@123");
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        when(passwordEncoder.encode("NewPassword@123")).thenReturn("EncodedNewPassword");
        when(guestRepository.save(any(Guest.class))).thenReturn(guest);
        when(guestMapper.guestToGuestDetailDto(any(Guest.class))).thenReturn(new GuestDetailDto("UpdatedJohn", "UpdatedDoe", "test@example.com", null, null, null, null, null, null, null, "EncodedNewPassword"));

        GuestDetailDto result = guestService.update("test@example.com", updateDto);

        assertAll(
            () -> assertNotNull(result),
            () -> assertEquals("UpdatedJohn", result.firstName()),
            () -> assertEquals("EncodedNewPassword", result.password()),
            () -> verify(guestRepository).save(any(Guest.class))
        );
    }

    @Test
    void testDeleteByEmail_withValidData_shouldDeleteGuest() throws ValidationException {
        // Arrange
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        when(bookingRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        // Act
        guestService.deleteByEmail("test@example.com");

        // Assert
        assertAll(
            () -> verify(guestRepository).deleteByEmail("test@example.com"),
            () -> verify(guestValidator).validateForDelete(any(Guest.class), anyList())
        );
    }

    @Test
    void testDeleteByEmail_withActiveBookings_shouldThrowValidationException() throws ValidationException {
        // Arrange
        Guest guest = new Guest();
        guest.setId(1L);
        guest.setEmail("test@example.com");

        Booking activeBooking = Booking.BookingBuilder.aBooking()
            .withStartDate(LocalDate.now().minusDays(1))
            .withEndDate(LocalDate.now().plusDays(5))
            .withUser(guest)
            .build();

        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(activeBooking));

        // Mock the validator to throw the ValidationException
        doThrow(new ValidationException("Validation failed", List.of("Cannot delete guest with active bookings.")))
            .when(guestValidator).validateForDelete(eq(guest), anyList());

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> guestService.deleteByEmail("test@example.com"));

        // Validate the exception
        assertAll(
            () -> assertNotNull(exception),
            () -> assertTrue(exception.getMessage().contains("Cannot delete guest with active bookings."))
        );

        // Ensure the repository delete method was never called
        verify(guestRepository, never()).deleteByEmail(anyString());
    }


    @Test
    void testDeleteGuestWithFutureBooking_shouldThrowValidationException() throws ValidationException {
        // Mock a guest
        Guest mockGuest = new Guest();
        mockGuest.setId(1L);
        mockGuest.setEmail("test@example.com");

        // Mock a booking with a future start date
        Booking futureBooking = Booking.BookingBuilder.aBooking()
            .withStartDate(LocalDate.now().plusDays(5))
            .withEndDate(LocalDate.now().plusDays(10))
            .withUser(mockGuest)
            .build();

        // Mock repository behavior
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockGuest));
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(futureBooking));

        // Mock the validator to throw a ValidationException
        doThrow(new ValidationException("Validation failed", List.of("Cannot delete guest with active bookings.")))
            .when(guestValidator).validateForDelete(eq(mockGuest), anyList());

        // Test
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            guestService.deleteByEmail("test@example.com");
        });

        // Validate the exception
        assertTrue(exception.getMessage().contains("Cannot delete guest with active bookings."));
        assertAll(
            () -> assertNotNull(exception)
        );
    }



}
