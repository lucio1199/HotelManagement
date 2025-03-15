package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ActivityBookingMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleActivityBookingService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ActivityBookingValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ActivityBookingServiceTest {

    @Mock
    private ActivityBookingRepository bookingRepository;

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivitySlotRepository activitySlotRepository;

    @Mock
    private ActivityBookingMapper activityBookingMapper;

    @Mock
    private UserService userService;

    @Mock
    private ActivityBookingValidator activityBookingValidator;

    @InjectMocks
    private SimpleActivityBookingService bookingService;

    private ApplicationUser testUser;
    private Activity testActivity;
    private ActivitySlot testSlot;
    private ActivityBooking testBooking;

    @BeforeEach
    public void setUp() {
        testUser = new ApplicationUser();
        testUser.setId(1L);
        testUser.setEmail("testuser@example.com");

        testActivity = new Activity();
        testActivity.setId(1L);
        testActivity.setName("Test Activity");
        testActivity.setPrice(50.0);
        testActivity.setCapacity(10);

        testSlot = new ActivitySlot();
        testSlot.setId(1L);
        testSlot.setActivity(testActivity);
        testSlot.setDate(LocalDate.now());
        testSlot.setOccupied(0);
        testSlot.setCapacity(10);

        testBooking = new ActivityBooking();
        testBooking.setId(1L);
        testBooking.setUser(testUser);
        testBooking.setActivity(testActivity);
        testBooking.setActivitySlot(testSlot);
        testBooking.setBookingDate(LocalDate.now());
        testBooking.setParticipants(3);
        testBooking.setStatus(BookingStatus.PENDING);
    }

    @Test
    public void createBooking_Success() throws NotFoundException, ValidationException {
        ActivityBookingCreateDto createDto = new ActivityBookingCreateDto(1L, 1L, LocalDate.now(), 3, "testuser@example.com");

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        when(activityRepository.findActivityById(1L)).thenReturn(Optional.of(testActivity));
        when(activitySlotRepository.findById(1L)).thenReturn(Optional.of(testSlot));
        when(bookingRepository.save(any(ActivityBooking.class))).thenReturn(testBooking);
        when(activityBookingMapper.activityBookingToActivityBookingDto(any(ActivityBooking.class), any(Activity.class)))
            .thenReturn(new ActivityBookingDto(1L, 1L, "Test Activity", LocalDate.now(), null, null, LocalDate.now(), 3, false));

        ActivityBookingDto result = bookingService.createBooking(createDto, "testuser@example.com");

        assertNotNull(result);
        assertEquals("Test Activity", result.activityName());
        verify(bookingRepository, times(1)).save(any(ActivityBooking.class));
        verify(activitySlotRepository, times(1)).save(testSlot);
    }

    @Test
    public void createBooking_UserNotFound() {
        ActivityBookingCreateDto createDto = new ActivityBookingCreateDto(1L, 1L, LocalDate.now(), 3, "nonexistent@example.com");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(createDto, "nonexistent@example.com"));
    }

    @Test
    @Disabled
    public void createBooking_ActivityNotFound() {
        ActivityBookingCreateDto createDto = new ActivityBookingCreateDto(99L, 1L, LocalDate.now(), 3, "testuser@example.com");

        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        when(activityRepository.findActivityById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.createBooking(createDto, "testuser@example.com"));
    }

    @Test
    public void updatePaymentStatus_Success() throws NotFoundException {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));

        testBooking.setStripeSessionId("mockSession");
        testBooking.setPaid(true);
        testBooking.setStatus(BookingStatus.ACTIVE);

        bookingService.updatePaymentStatus(1L);

        verify(bookingRepository, times(1)).save(testBooking);
    }

    @Test
    public void updatePaymentStatus_BookingNotFound() {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.updatePaymentStatus(999L));
    }

    @Test
    public void findByUserEmail_Success() throws NotFoundException {
        when(userRepository.findByEmail("testuser@example.com")).thenReturn(Optional.of(testUser));
        when(bookingRepository.findActiveBookingsByUserId(1L)).thenReturn(List.of(testBooking));
        when(activityBookingMapper.activityBookingsToActivityBookingDtos(anyList()))
            .thenReturn(List.of(new ActivityBookingDto(1L, 1L, "Test Activity", LocalDate.now(), null, null, LocalDate.now(), 3, false)));

        List<ActivityBookingDto> result = bookingService.findByUserEmail("testuser@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Activity", result.get(0).activityName());
        verify(bookingRepository, times(1)).findActiveBookingsByUserId(1L);
    }

    @Test
    public void findByUserEmail_UserNotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.findByUserEmail("nonexistent@example.com"));
    }
}
