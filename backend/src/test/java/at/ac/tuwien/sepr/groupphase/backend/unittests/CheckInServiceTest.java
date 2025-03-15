package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.BookingMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.CheckInMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.GuestMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.RoomMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.*;
import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.BookingService;
import at.ac.tuwien.sepr.groupphase.backend.service.MailService;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleCheckInService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.CheckInValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.CheckedInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CheckInServiceTest {

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private CheckOutRepository checkOutRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private GuestRepository guestRepository;

    @Mock
    private CheckInValidator checkInValidator;

    @InjectMocks
    private SimpleCheckInService checkInService;

    @Mock
    private MultipartFile passport;

    @Mock
    private RoomMapper roomMapper;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CheckInMapper checkInMapper;

    @Mock
    private GuestMapper guestMapper;

    @Mock
    private BookingService bookingService;

    @Mock
    private InviteToRoomRepository inviteToRoomRepository;

    @Mock
    private MailService mailService;

    @Test
    public void givenValidCheckInDto_whenCheckIn_thenSave() throws Exception {
        CheckInDto checkInDto = new CheckInDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789");

        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);

        DetailedBookingDto bookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13"), 2500.5 , 3, "");

        Guest guest = new Guest();
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        MultipartFile passport = mock(MultipartFile.class);
        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.bookingToDetailedBookingDto(booking)).thenReturn(bookingDto);
        when(checkInMapper.checkInDtoToCheckIn(checkInDto, booking, user, passport)).thenReturn(checkIn);

        checkInService.checkIn(checkInDto, passport, "test@example.com");
        verify(checkInRepository, times(1)).save(any(CheckIn.class));
    }

    @Test
    public void givenInvalidGuestEmail_whenCheckIn_thenThrowNotFoundException() {
        String invalidEmail = "invalid@example.com";

        CheckInDto checkInDto = new CheckInDto(1L, "John", "Doe", null, "Vienna", Gender.MALE, Nationality.AUT,
            "Main St. 1", "P123456", "0123456789");

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> checkInService.checkIn(checkInDto, passport, invalidEmail));
        assertEquals("Guest with email invalid@example.com not found", exception.getMessage());
    }

    @Test
    public void givenValidEmail_whenGetGuestRoom_thenReturnRoom() throws NotFoundException {
        String validEmail = "test@example.com";
        ApplicationUser user = new ApplicationUser();
        user.setEmail(validEmail);
        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        room.setDescription("Room Description");
        room.setPrice(1.0);

        DetailedRoomDto roomDto = new DetailedRoomDto(1L, "Room Name", "Room Description", 1.0,
            1, null, List.of(), null);
        CheckIn checkIn = new CheckIn();

        when(userRepository.findByEmail(validEmail)).thenReturn(Optional.of(user));
        when(checkInRepository.findCheckInByGuestOrderByDateDesc(user)).thenReturn(Optional.of(List.of(checkIn)));
        List<CheckIn> checkIns = new ArrayList<>();
        checkIns.add(checkIn);
        List<CheckOut> checkOuts = new ArrayList<>();
        when(checkInRepository.findCheckInByBookingAndGuest(null, user)).thenReturn(checkIns);
        when(checkOutRepository.findCheckOutByBookingAndGuest(null, user)).thenReturn(checkOuts);
        when(roomMapper.roomToDetailedRoomDto(null, null)).thenReturn(roomDto);

        DetailedRoomDto[] result = checkInService.getGuestRooms(validEmail);

        assertNotNull(result);
        assertEquals("Room Name", result[result.length - 1].name());
    }

    @Test
    public void givenInvalidEmail_whenCheckOut_thenThrowNotFoundException() throws ValidationException {
        Booking booking = new Booking();
        booking.setId(1L);

        CheckOutDto checkOutDto = new CheckOutDto(1L, "someone@else.com");

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> checkInService.checkOut(checkOutDto));
        assertEquals("Booking with id 1 not found", exception.getMessage());
        verify(bookingRepository).findBookingById(1L);
    }

    @Test
    public void multipleBookingsGivenValidCheckInDto_whenCheckIn_thenSave() throws Exception {
        CheckInDto checkInDto = new CheckInDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789");
        CheckInDto checkInDto2 = new CheckInDto(2L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789");

        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        Booking booking2 = new Booking();
        booking2.setId(2L);

        DetailedBookingDto bookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13"), 2500.5 , 5, "");
        DetailedBookingDto bookingDto2 = new DetailedBookingDto(2L, 2L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13") , 2500.5, 3, "");

        Guest guest = new Guest();
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        Room room2 = new Room();
        room2.setId(2L);
        room2.setName("Room Name 2");

        MultipartFile passport = mock(MultipartFile.class);
        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        CheckIn checkIn2 = new CheckIn();
        checkIn2.setId(2L);
        checkIn2.setDate(LocalDateTime.now());
        checkIn2.setBooking(booking2);
        checkIn2.setGuest(guest);
        checkIn2.setPassport(pdfContent);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findBookingById(2L)).thenReturn(Optional.of(booking2));
        when(bookingMapper.bookingToDetailedBookingDto(booking)).thenReturn(bookingDto);
        when(bookingMapper.bookingToDetailedBookingDto(booking2)).thenReturn(bookingDto2);
        when(checkInMapper.checkInDtoToCheckIn(checkInDto, booking, user, passport)).thenReturn(checkIn);
        when(checkInMapper.checkInDtoToCheckIn(checkInDto2, booking2, user, passport)).thenReturn(checkIn2);

        checkInService.checkIn(checkInDto, passport, "test@example.com");
        checkInService.checkIn(checkInDto2, passport, "test@example.com");
        verify(checkInRepository, times(2)).save(any(CheckIn.class));
    }

    @Test
    public void whenCheckedIn_thenGetAllBookingIds_returnsList() throws Exception {
        CheckInDto checkInDto = new CheckInDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789");
        CheckInDto checkInDto2 = new CheckInDto(2L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789");

        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        Booking booking2 = new Booking();
        booking2.setId(2L);

        DetailedBookingDto bookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13") , 2500.5, 5, "");
        DetailedBookingDto bookingDto2 = new DetailedBookingDto(2L, 2L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD9E9C0", LocalDate.parse("2025-01-13") , 2500.5, 5, "");

        Guest guest = new Guest();
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        Room room2 = new Room();
        room2.setId(2L);
        room2.setName("Room Name 2");

        MultipartFile passport = mock(MultipartFile.class);
        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        CheckIn checkIn2 = new CheckIn();
        checkIn2.setId(2L);
        checkIn2.setDate(LocalDateTime.now());
        checkIn2.setBooking(booking2);
        checkIn2.setGuest(guest);
        checkIn2.setPassport(pdfContent);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findBookingById(2L)).thenReturn(Optional.of(booking2));
        when(bookingMapper.bookingToDetailedBookingDto(booking)).thenReturn(bookingDto);
        when(bookingMapper.bookingToDetailedBookingDto(booking2)).thenReturn(bookingDto2);
        when(checkInMapper.checkInDtoToCheckIn(checkInDto, booking, user, passport)).thenReturn(checkIn);
        when(checkInMapper.checkInDtoToCheckIn(checkInDto2, booking2, user, passport)).thenReturn(checkIn2);

        checkInService.checkIn(checkInDto, passport, "test@example.com");
        checkInService.checkIn(checkInDto2, passport, "test@example.com");
        verify(checkInRepository, times(2)).save(any(CheckIn.class));

        List<Long> mockBookingIds = List.of(1L, 2L);
        when(checkInRepository.findAllBookingIds()).thenReturn(mockBookingIds);
        List<Long> result = checkInService.getAllBookingIds();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void whenBookingExists_findBookingById_returnsBooking() throws Exception {
        CheckInDto checkInDto = new CheckInDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789");
        CheckInDto checkInDto2 = new CheckInDto(2L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789");

        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);
        Booking booking2 = new Booking();
        booking2.setId(2L);

        DetailedBookingDto bookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13"), 2500.5 , 5, "");
        DetailedBookingDto bookingDto2 = new DetailedBookingDto(2L, 2L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13") , 2500.5, 5, "");

        Guest guest = new Guest();
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        Room room2 = new Room();
        room2.setId(2L);
        room2.setName("Room Name 2");

        MultipartFile passport = mock(MultipartFile.class);
        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        CheckIn checkIn2 = new CheckIn();
        checkIn2.setId(2L);
        checkIn2.setDate(LocalDateTime.now());
        checkIn2.setBooking(booking2);
        checkIn2.setGuest(guest);
        checkIn2.setPassport(pdfContent);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findBookingById(2L)).thenReturn(Optional.of(booking2));
        when(bookingMapper.bookingToDetailedBookingDto(booking)).thenReturn(bookingDto);
        when(bookingMapper.bookingToDetailedBookingDto(booking2)).thenReturn(bookingDto2);
        when(checkInMapper.checkInDtoToCheckIn(checkInDto, booking, user, passport)).thenReturn(checkIn);
        when(checkInMapper.checkInDtoToCheckIn(checkInDto2, booking2, user, passport)).thenReturn(checkIn2);
        when(inviteToRoomRepository.findInviteToRoomByGuest(guest)).thenReturn(Optional.empty());

        checkInService.checkIn(checkInDto, passport, "test@example.com");
        checkInService.checkIn(checkInDto2, passport, "test@example.com");
        verify(checkInRepository, times(2)).save(any(CheckIn.class));

        when(bookingService.findBookingById(booking.getId())).thenReturn(bookingDto);
        DetailedBookingDto result = checkInService.findBookingById(booking.getId(), 1L);

        assertNotNull(result);
        assertEquals(bookingDto, result);
    }

    @Test

    public void whenNotCheckedIn_getGuestBooking_throwsNotFoundException() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new ApplicationUser()));
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> checkInService.getGuestBooking(1L, "test@example.com"));
        assertEquals("Booking for guest test@example.com not found.", exception.getMessage());
    }

    @Test
    public void givenValidAddToRoomDto_whenAddToRoom_thenSave() throws Exception {
        AddToRoomDto addToRoomDto = new AddToRoomDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789", "test2@example.com");

        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        ApplicationUser user2 = new ApplicationUser();
        user2.setEmail("test2@example.com");
        user2.setId(2L);

        Booking booking = new Booking();
        booking.setId(1L);

        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        Guest guest2 = new Guest();
        guest2.setEmail("test2@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        DetailedRoomDto roomDto = new DetailedRoomDto(1L, "name", "desc", 0.0, 4,
            "", List.of(""), null);
        DetailedBookingDto bookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, "",
            "", LocalDate.now(), 1.0d, 7, "");

        MultipartFile passport = mock(MultipartFile.class);
        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        CheckInDto checkInDto = new CheckInDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("test2@example.com")).thenReturn(Optional.of(user2));
        when(guestRepository.findByEmail("test2@example.com")).thenReturn(Optional.of(guest2));
        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.bookingToDetailedBookingDto(booking)).thenReturn(bookingDto);
        when(roomMapper.roomToDetailedRoomDto(room, null)).thenReturn(roomDto);
        when(roomRepository.findRoomById(1L)).thenReturn(room);
        when(checkInRepository.findCheckInByBooking(booking)).thenReturn(List.of(checkIn));
        when(checkInMapper.checkInDtoToCheckIn(checkInDto, booking, user2, passport)).thenReturn(checkIn);

        checkInService.addToRoom(addToRoomDto, passport, "test@example.com");
        verify(checkInRepository, times(1)).save(any(CheckIn.class));
    }

    @Test
    public void givenNotCheckedInOwner_whenAddToRoom_thenThrowNotFoundException() throws Exception {
        AddToRoomDto addToRoomDto = new AddToRoomDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789", "test2@example.com");

        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        Booking booking = new Booking();
        booking.setId(1L);

        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        Guest guest2 = new Guest();
        guest2.setEmail("test2@example.com");



        DetailedRoomDto roomDto = new DetailedRoomDto(1L, "name", "desc", 0.0, 4,
            "", List.of(""), null);
        DetailedBookingDto bookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, "",
            "", LocalDate.now(), 1.0d, 7, "");


        when(guestRepository.findByEmail("test2@example.com")).thenReturn(Optional.of(guest2));
        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.bookingToDetailedBookingDto(booking)).thenReturn(bookingDto);
        when(checkInRepository.findCheckInByBooking(booking)).thenReturn(null);

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> checkInService.addToRoom(addToRoomDto, passport, "test@example.com"));
        assertEquals("Cannot add guests to a room since you are not checked in.", exception.getMessage());
    }

    @Test
    public void givenValidId_whenGetGuests_thenReturnGuests() throws Exception {
        AddToRoomDto addToRoomDto = new AddToRoomDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789", "test2@example.com");

        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        ApplicationUser user2 = new ApplicationUser();
        user2.setEmail("test2@example.com");
        user2.setId(2L);

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        room.setCapacity(2);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);

        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        Guest guest2 = new Guest();
        guest2.setEmail("test2@example.com");

        DetailedRoomDto roomDto = new DetailedRoomDto(1L, "name", "desc", 0.0, 4,
            "", List.of(""), null);
        DetailedBookingDto bookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, "",
            "", LocalDate.now(), 1.0d, 7, "");

        MultipartFile passport = mock(MultipartFile.class);
        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        CheckIn checkIn2 = new CheckIn();
        checkIn2.setId(2L);
        checkIn2.setDate(LocalDateTime.now());
        checkIn2.setBooking(booking);
        checkIn2.setGuest(guest2);
        checkIn2.setPassport(pdfContent);

        CheckInStatusDto[] checkInStatusDtos = new CheckInStatusDto[]{new CheckInStatusDto(1L, "test@example.com")};
        List<CheckInStatusDto> checkInStatusList = List.of(checkInStatusDtos);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        when(guestRepository.findByEmail("test2@example.com")).thenReturn(Optional.of(guest2));
        when(checkInRepository.findCheckInByBooking(booking)).thenReturn(List.of(checkIn, checkIn2));
        when(checkInMapper.checkInsToCheckInStatusDtos(List.of())).thenReturn(checkInStatusList);
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(booking));
        when(guestMapper.guestToGuestListDto(guest2)).thenReturn(new GuestListDto("John", "Doe", "test2@example.com"));

        GuestListDto[] guestsActual = checkInService.getGuests(1L, "test@example.com");
        int guestsExpectedLength = 2;
        assertEquals(guestsExpectedLength, guestsActual.length);
        String guestsExpectedFirstName = "John";
        assertEquals(guestsExpectedFirstName, guestsActual[0].firstName());
    }

    @Test
    public void givenInvalidId_whenGetGuests_thenReturnGuests() throws Exception {
        AddToRoomDto addToRoomDto = new AddToRoomDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789", "test2@example.com");

        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        ApplicationUser user2 = new ApplicationUser();
        user2.setEmail("test2@example.com");
        user2.setId(2L);

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        room.setCapacity(2);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);

        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        Guest guest2 = new Guest();
        guest2.setEmail("test2@example.com");

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);

        CheckIn checkIn2 = new CheckIn();
        checkIn2.setId(2L);
        checkIn2.setDate(LocalDateTime.now());
        checkIn2.setBooking(booking);
        checkIn2.setGuest(guest2);

        CheckInStatusDto[] checkInStatusDtos = new CheckInStatusDto[]{new CheckInStatusDto(1L, "test@example.com")};
        List<CheckInStatusDto> checkInStatusList = List.of(checkInStatusDtos);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(checkInMapper.checkInsToCheckInStatusDtos(List.of())).thenReturn(checkInStatusList);
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(booking));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> checkInService.getGuests(-1L, "test@example.com"));
        assertEquals("No extra checked in guests for room found", exception.getMessage());
    }

    @Test
    public void whenNoBooking_isOwner_returnFalse() throws Exception {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new ApplicationUser()));
        boolean actual = checkInService.isOwner(1L, "test@example.com");
        assertFalse(actual);
    }

    @Test
    public void whenBooking_isOwner_returnTrue() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking(room, user, LocalDate.now(), LocalDate.now().plusDays(7), false, BookingStatus.PENDING);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findBookingsByRoomIdAndStartDateBetween(room.getId(), LocalDate.now(), LocalDate.now())).thenReturn(List.of(booking));
        boolean actual = checkInService.isOwner(1L, "test@example.com");
        assertTrue(actual);
    }

    @Test
    public void whenCheckInExists_getOccupancyStatus_returnsOccupied() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);
        Guest guest = new Guest();
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);

        CheckInStatusDto statusDto = new CheckInStatusDto(booking.getId(), guest.getEmail());
        CheckInStatusDto[] statusDtos = new CheckInStatusDto[1];
        statusDtos[0] = statusDto;

        when(bookingRepository.findBookingsByRoomIdAndStartDateBetween(room.getId(), LocalDate.now(), LocalDate.now())).thenReturn(List.of(booking));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(checkInMapper.checkInsToCheckInStatusDtos(List.of())).thenReturn(List.of(statusDtos));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        OccupancyDto result = checkInService.getOccupancyStatus(room.getId());
        OccupancyDto expected = new OccupancyDto(room.getId(), "occupied");
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void whenNoCheckInExists_getOccupancyStatus_returnsNotOccupied() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);
        Guest guest = new Guest();
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);

        CheckInStatusDto statusDto = new CheckInStatusDto(booking.getId(), guest.getEmail());
        CheckInStatusDto[] statusDtos = new CheckInStatusDto[1];
        statusDtos[0] = statusDto;

        when(bookingRepository.findBookingsByRoomIdAndStartDateBetween(room.getId(), LocalDate.now(), LocalDate.now())).thenReturn(List.of(booking));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        OccupancyDto result = checkInService.getOccupancyStatus(room.getId());
        OccupancyDto expected = new OccupancyDto(room.getId(), "not-occupied");
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void whenOwnerGuestsExists_getAllGuests_returnsGuest() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);
        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        guest.setFirstName("John");
        guest.setLastName("Doe");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);


        MultipartFile passport = mock(MultipartFile.class);
        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        CheckInStatusDto statusDto = new CheckInStatusDto(booking.getId(), guest.getEmail());
        CheckInStatusDto[] statusDtos = new CheckInStatusDto[1];
        statusDtos[0] = statusDto;

        GuestListDto guestListDto = new GuestListDto(guest.getFirstName(), guest.getLastName(), guest.getEmail());

        when(bookingRepository.findBookingById(booking.getId())).thenReturn(Optional.of(booking));
        when(checkInRepository.findCheckInByBooking(booking)).thenReturn(List.of(checkIn));
        when(guestRepository.findByEmail(guest.getEmail())).thenReturn(Optional.of(guest));
        when(guestMapper.guestToGuestListDto(guest)).thenReturn(guestListDto);

        GuestListDto[] result = checkInService.getAllGuests(booking.getId());

        GuestListDto[] expected = new GuestListDto[1];
        expected[0] = guestListDto;
        assertNotNull(result);
        assertEquals(expected[0], result[0]);
    }

    @Test
    public void whenNoOwnerGuestsExists_getAllGuests_throwsNotFoundException() throws Exception {
        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setRoom(room);

        when(bookingRepository.findBookingById(booking.getId())).thenReturn(Optional.of(booking));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> checkInService.getAllGuests(booking.getId()));
        assertEquals("No checked in guests for room found", exception.getMessage());
    }

    @Test
    public void whenGuestExistsAndIsCheckedIn_getPassportByBookingIdAndEmail_returnsPassport() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);
        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        guest.setFirstName("John");
        guest.setLastName("Doe");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);

        MultipartFile passport = mock(MultipartFile.class);
        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(bookingRepository.findBookingById(booking.getId())).thenReturn(Optional.of(booking));
        when(checkInRepository.findCheckInByBookingAndGuest(booking, user)).thenReturn(List.of(checkIn));

        byte[] result = checkInService.getPassportByBookingIdAndEmail(booking.getId(), user.getEmail());

        assertEquals(pdfContent, result);
    }

    @Test
    public void whenGuestExistsAndIsNotCheckedIn_getPassportByBookingIdAndEmail_throwsNotFoundException() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);
        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        guest.setFirstName("John");
        guest.setLastName("Doe");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(bookingRepository.findBookingById(booking.getId())).thenReturn(Optional.of(booking));

        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> checkInService.getPassportByBookingIdAndEmail(booking.getId(), user.getEmail()));
        assertEquals("Check-In for booking with id 1 and guest with email test@example.com not found", exception.getMessage());
    }

    @Test
    public void whenCheckInExistsAndBookingIsRunningOut_performAutoCheckOut_returns1() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);
        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        guest.setFirstName("John");
        guest.setLastName("Doe");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setEndDate(LocalDate.now());

        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        when(bookingRepository.findBookingsByEndDate(LocalDate.now())).thenReturn(Optional.of(List.of(booking)));
        when(checkInRepository.findCheckInByBooking(booking)).thenReturn(List.of(checkIn));
        when(checkOutRepository.findCheckOutByBookingAndGuest(booking, guest)).thenReturn(List.of());

        int result = checkInService.performAutoCheckOut();
        int expected = 1;
        assertEquals(expected, result);
    }

    @Test
    public void whenNoCheckInExistsAndBookingIsRunningOut_performAutoCheckOut_returns0() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);
        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        guest.setFirstName("John");
        guest.setLastName("Doe");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setEndDate(LocalDate.now());

        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        when(bookingRepository.findBookingsByEndDate(LocalDate.now())).thenReturn(Optional.of(List.of(booking)));
        when(checkInRepository.findCheckInByBooking(booking)).thenReturn(List.of());

        int result = checkInService.performAutoCheckOut();
        int expected = 0;
        assertEquals(expected, result);
    }

    @Test
    public void whenCheckInExistsAndBookingRanOutYesterday_performAutoCheckOut_returns0() throws Exception {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);
        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        guest.setFirstName("John");
        guest.setLastName("Doe");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        booking.setRoom(room);
        booking.setEndDate(LocalDate.now().minusDays(1L));

        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        when(bookingRepository.findBookingsByEndDate(LocalDate.now())).thenReturn(Optional.of(List.of(booking)));
        when(checkInRepository.findCheckInByBooking(booking)).thenReturn(List.of(checkIn));
        when(checkOutRepository.findCheckOutByBookingAndGuest(booking, guest)).thenReturn(List.of());

        int result = checkInService.performAutoCheckOut();
        int expected = 1;
        assertEquals(expected, result);
    }

    @Test
    public void givenValidEmail_whenRemove_thenReturn() {
        Booking booking = new Booking();
        booking.setId(1L);

        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");

        CheckIn checkIn = new CheckIn(booking, LocalDateTime.now(), user, null);

        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(checkInRepository.findCheckInByBookingAndGuest(booking, user)).thenReturn(List.of(checkIn));

        assertDoesNotThrow(() -> checkInService.remove(1L, "test@example.com"));
        verify(bookingRepository).findBookingById(1L);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    public void givenInvalidEmail_whenRemove_thenThrowNotFoundException() {
        Booking booking = new Booking();
        booking.setId(1L);

        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("not_found@example.com")).thenReturn(Optional.empty());

        // Act & Assert: Ausnahme wird aufgrund der fehlenden E-Mail erwartet
        NotFoundException exception = assertThrows(NotFoundException.class,
            () -> checkInService.remove(1L, "not_found@example.com"));
        assertEquals("Guest with email not_found@example.com not found", exception.getMessage());

        // Verifizieren, dass die Buchung zuerst geprüft wurde
        verify(bookingRepository).findBookingById(1L);

        // Verifizieren, dass die E-Mail geprüft wurde, nachdem die Buchung validiert war
        verify(userRepository).findByEmail("not_found@example.com");
    }

    @Test
    public void whenNoInviteExists_inviteToRoom_returns() throws Exception {
        CheckInDto checkInDto = new CheckInDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789");
        CheckInDto checkInDto2 = new CheckInDto(2L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789");

        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        ApplicationUser user2 = new ApplicationUser();
        user.setEmail("test2@example.com");
        user.setId(2L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        Booking booking2 = new Booking();
        booking2.setId(2L);

        DetailedBookingDto bookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13"), 2500.5 , 5, "");
        DetailedBookingDto bookingDto2 = new DetailedBookingDto(2L, 2L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13") , 2500.5, 5, "");

        Guest guest = new Guest();
        guest.setEmail("test@example.com");

        Guest guest2 = new Guest();
        guest2.setEmail("test2@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        Room room2 = new Room();
        room2.setId(2L);
        room2.setName("Room Name 2");

        MultipartFile passport = mock(MultipartFile.class);
        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        CheckIn checkIn2 = new CheckIn();
        checkIn2.setId(2L);
        checkIn2.setDate(LocalDateTime.now());
        checkIn2.setBooking(booking2);
        checkIn2.setGuest(guest);
        checkIn2.setPassport(pdfContent);

        InviteToRoomDto inviteToRoomDto = new InviteToRoomDto(booking.getId(), "test2@example.com", "test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("test2@example.com")).thenReturn(Optional.of(user2));
        when(guestRepository.findByEmail("test@example.com")).thenReturn(Optional.of(guest));
        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.bookingToDetailedBookingDto(booking)).thenReturn(bookingDto);
        when(checkInMapper.checkInDtoToCheckIn(checkInDto, booking, user, passport)).thenReturn(checkIn);
        when(inviteToRoomRepository.findInviteToRoomByGuest(guest)).thenReturn(Optional.empty());

        checkInService.checkIn(checkInDto, passport, "test@example.com");

        when(bookingService.findBookingById(booking.getId())).thenReturn(bookingDto);
        DetailedBookingDto result = checkInService.findBookingById(booking.getId(), 1L);
        assertDoesNotThrow(() -> checkInService.inviteToRoom(inviteToRoomDto, "test@example.com"));
        assertNotNull(result);
        assertEquals(bookingDto, result);
    }

    @Test

    public void whenInviteExists_inviteToRoom_throwsConflictException() {
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        ApplicationUser user2 = new ApplicationUser();
        user.setEmail("test2@example.com");
        user.setId(2L);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user);
        Booking booking2 = new Booking();
        booking2.setId(2L);

        DetailedBookingDto bookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13"), 2500.5 , 5, "");

        Guest guest = new Guest();
        guest.setEmail("test@example.com");

        Room room = new Room();
        room.setId(1L);
        room.setName("Room Name");
        Room room2 = new Room();
        room2.setId(2L);
        room2.setName("Room Name 2");

        byte[] pdfContent = "Mock PDF Content".getBytes(); // Represent PDF content as byte array

        CheckIn checkIn = new CheckIn();
        checkIn.setId(1L);
        checkIn.setDate(LocalDateTime.now());
        checkIn.setBooking(booking);
        checkIn.setGuest(guest);
        checkIn.setPassport(pdfContent);

        CheckIn checkIn2 = new CheckIn();
        checkIn2.setId(2L);
        checkIn2.setDate(LocalDateTime.now());
        checkIn2.setBooking(booking2);
        checkIn2.setGuest(guest);
        checkIn2.setPassport(pdfContent);

        InviteToRoomDto inviteToRoomDto = new InviteToRoomDto(booking.getId(), "test2@example.com", "test@example.com");
        InviteToRoom inviteToRoom = new InviteToRoom(booking, user2);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bookingRepository.findBookingById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.bookingToDetailedBookingDto(booking)).thenReturn(bookingDto);
        when(inviteToRoomRepository.findInviteToRoomByGuestAndBooking(guest, booking)).thenReturn(Optional.of(inviteToRoom));

        inviteToRoomRepository.save(new InviteToRoom(booking, guest));

        when(userRepository.findByEmail("test2@example.com")).thenReturn(Optional.of(guest));
        ConflictException exception = assertThrows(ConflictException.class,
            () -> checkInService.inviteToRoom(inviteToRoomDto, "test@example.com"));
        assertEquals("Cannot invite guest to the room. Conflicts: The guest is already invited to the room.", exception.getMessage());
    }
}
