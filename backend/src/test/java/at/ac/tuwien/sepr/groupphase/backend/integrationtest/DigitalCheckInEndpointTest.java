package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;
import at.ac.tuwien.sepr.groupphase.backend.enums.RoleType;
import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.CheckInRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Slf4j
@Import(TestSecurityConfig.class)
public class DigitalCheckInEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApplicationUserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    @Qualifier("userDetailsServiceStub")
    private UserDetailsService userDetailsServiceStub;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        checkInRepository.deleteAll();
        bookingRepository.deleteAll();
        roomRepository.deleteAll();

        CHECK_IN_USER_GUEST.setEmail(CHECK_IN_USER);
        CHECK_IN_USER_GUEST.setPassword(passwordEncoder.encode("password"));
        CHECK_IN_USER_GUEST.setRoleType(RoleType.ROLE_GUEST);
        CHECK_IN_USER_GUEST.setVerified(true);
        CHECK_IN_USER_GUEST.setPhoneNumber("+1234");

        CHECK_IN_USER_GUEST.setFirstName("Test");
        CHECK_IN_USER_GUEST.setLastName("User");
        CHECK_IN_USER_GUEST.setDateOfBirth(LocalDate.parse("1990-01-" + String.format("%02d", 1)));
        CHECK_IN_USER_GUEST.setPlaceOfBirth("City");
        CHECK_IN_USER_GUEST.setGender(Gender.MALE);
        CHECK_IN_USER_GUEST.setNationality(Nationality.values()[1 % Nationality.values().length]);
        CHECK_IN_USER_GUEST.setAddress("123 Main St, City");
        CHECK_IN_USER_GUEST.setPassportNumber("P1234567");
        guestRepository.save(CHECK_IN_USER_GUEST);


        userService.login(new UserLoginDto(CHECK_IN_USER_GUEST.getEmail(), "password"));

        roomRepository.deleteAll();
    }

    /**
     * Test case: Successfully checks into a booking.
     */
    @Test
    @WithMockUser(username = CHECK_IN_USER, roles = {"GUEST"})
    public void testCheckIn_Success() throws Exception {
        Room room = Room.RoomBuilder.aRoom()
            .withName(TEST_ROOM_NAME)
            .withDescription(TEST_ROOM_DESCRIPTION)
            .withPrice(TEST_ROOM_PRICE)
            .withCapacity(TEST_ROOM_CAPACITY)
            .withMainImage(TEST_ROOM_MAIN_IMAGE)
            .withCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime())
            .build();
        roomRepository.save(room);

        Booking CHECK_IN_BOOKING = Booking.BookingBuilder.aBooking()
            .withUser(CHECK_IN_USER_GUEST)
            .withRoom(room)
            .withStartDate(LocalDate.now())
            .withEndDate(LocalDate.now().plusDays(7))
            .build();
        Long CHECK_IN_BOOKING_ID = bookingRepository.save(CHECK_IN_BOOKING).getId();

        // Create CheckInDto with test data
        CheckInDto checkInDto = new CheckInDto(
            CHECK_IN_BOOKING_ID,
            CHECK_IN_USER_GUEST.getFirstName(),
            CHECK_IN_USER_GUEST.getLastName(),
            CHECK_IN_USER_GUEST.getDateOfBirth(),
            CHECK_IN_USER_GUEST.getPlaceOfBirth(),
            CHECK_IN_USER_GUEST.getGender(),
            CHECK_IN_USER_GUEST.getNationality(),
            CHECK_IN_USER_GUEST.getAddress(),
            CHECK_IN_USER_GUEST.getPassportNumber(),
            CHECK_IN_USER_GUEST.getPhoneNumber()
        );

        // Mock passport file
        MockMultipartFile passportFile = new MockMultipartFile(
            "passport",
            "passport.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "mock-passport-content".getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, CHECK_IN_BASE_URI)
                    .file(passportFile)
                    .param("bookingId", String.valueOf(checkInDto.bookingId()))
                    .param("firstName", checkInDto.firstName())
                    .param("lastName", checkInDto.lastName())
                    .param("dateOfBirth", checkInDto.dateOfBirth().toString())
                    .param("placeOfBirth", checkInDto.placeOfBirth())
                    .param("gender", checkInDto.gender().toString())
                    .param("nationality", checkInDto.nationality().toString())
                    .param("address", checkInDto.address())
                    .param("passportNumber", checkInDto.passportNumber())
                    .param("phoneNumber", checkInDto.phoneNumber())
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isCreated());
    }

    /**
     * Test case: Fails to check into a non existing booking.
     */
    @Test
    @WithMockUser(username = CHECK_IN_USER, roles = {"GUEST"})
    public void testCheckIn_NonExisting() throws Exception {
        Room room = Room.RoomBuilder.aRoom()
            .withName(TEST_ROOM_NAME)
            .withDescription(TEST_ROOM_DESCRIPTION)
            .withPrice(TEST_ROOM_PRICE)
            .withCapacity(TEST_ROOM_CAPACITY)
            .withMainImage(TEST_ROOM_MAIN_IMAGE)
            .withCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime())
            .build();
        roomRepository.save(room);

        Booking CHECK_IN_BOOKING = Booking.BookingBuilder.aBooking()
            .withUser(CHECK_IN_USER_GUEST)
            .withRoom(room)
            .withStartDate(LocalDate.now())
            .withEndDate(LocalDate.now().plusDays(7))
            .build();
        Long CHECK_IN_BOOKING_ID = -1L;  // non existing booking

        // Create CheckInDto with test data
        CheckInDto checkInDto = new CheckInDto(
            CHECK_IN_BOOKING_ID,
            CHECK_IN_USER_GUEST.getFirstName(),
            CHECK_IN_USER_GUEST.getLastName(),
            CHECK_IN_USER_GUEST.getDateOfBirth(),
            CHECK_IN_USER_GUEST.getPlaceOfBirth(),
            CHECK_IN_USER_GUEST.getGender(),
            CHECK_IN_USER_GUEST.getNationality(),
            CHECK_IN_USER_GUEST.getAddress(),
            CHECK_IN_USER_GUEST.getPassportNumber(),
            CHECK_IN_USER_GUEST.getPhoneNumber()
        );

        // Mock passport file
        MockMultipartFile passportFile = new MockMultipartFile(
            "passport",
            "passport.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "mock-passport-content".getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, CHECK_IN_BASE_URI)
                    .file(passportFile)
                    .param("bookingId", String.valueOf(checkInDto.bookingId()))
                    .param("firstName", checkInDto.firstName())
                    .param("lastName", checkInDto.lastName())
                    .param("dateOfBirth", checkInDto.dateOfBirth().toString())
                    .param("placeOfBirth", checkInDto.placeOfBirth())
                    .param("gender", checkInDto.gender().toString())
                    .param("nationality", checkInDto.nationality().toString())
                    .param("address", checkInDto.address())
                    .param("passportNumber", checkInDto.passportNumber())
                    .param("phoneNumber", checkInDto.phoneNumber())
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isNotFound());
    }

    /**
     * Test case: Fails to check into a booking when providing incorrect guest information.
     */
    @Test
    @WithMockUser(username = CHECK_IN_USER, roles = {"GUEST"})
    public void testCheckIn_ValidationProblems() throws Exception {
        Room room = Room.RoomBuilder.aRoom()
            .withName(TEST_ROOM_NAME)
            .withDescription(TEST_ROOM_DESCRIPTION)
            .withPrice(TEST_ROOM_PRICE)
            .withCapacity(TEST_ROOM_CAPACITY)
            .withMainImage(TEST_ROOM_MAIN_IMAGE)
            .withCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime())
            .build();
        roomRepository.save(room);

        Booking CHECK_IN_BOOKING = Booking.BookingBuilder.aBooking()
            .withUser(CHECK_IN_USER_GUEST)
            .withRoom(room)
            .withStartDate(LocalDate.now())
            .withEndDate(LocalDate.now().plusDays(7))
            .build();
        Long CHECK_IN_BOOKING_ID = bookingRepository.save(CHECK_IN_BOOKING).getId();

        // Create CheckInDto with test data
        CheckInDto checkInDto = new CheckInDto(
            CHECK_IN_BOOKING_ID,
            "a",  // incorrect data
            "",  // incorrect data
            CHECK_IN_USER_GUEST.getDateOfBirth(),
            CHECK_IN_USER_GUEST.getPlaceOfBirth(),
            CHECK_IN_USER_GUEST.getGender(),
            CHECK_IN_USER_GUEST.getNationality(),
            CHECK_IN_USER_GUEST.getAddress(),
            CHECK_IN_USER_GUEST.getPassportNumber(),
            CHECK_IN_USER_GUEST.getPhoneNumber()
        );

        // Mock passport file
        MockMultipartFile passportFile = new MockMultipartFile(
            "passport",
            "passport.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "mock-passport-content".getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, CHECK_IN_BASE_URI)
                    .file(passportFile)
                    .param("bookingId", String.valueOf(checkInDto.bookingId()))
                    .param("firstName", checkInDto.firstName())
                    .param("lastName", checkInDto.lastName())
                    .param("dateOfBirth", checkInDto.dateOfBirth().toString())
                    .param("placeOfBirth", checkInDto.placeOfBirth())
                    .param("gender", checkInDto.gender().toString())
                    .param("nationality", checkInDto.nationality().toString())
                    .param("address", checkInDto.address())
                    .param("passportNumber", checkInDto.passportNumber())
                    .param("phoneNumber", checkInDto.phoneNumber())
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Test case: Fails to check into a booking when providing nonsensical date of birth.
     */
    @Test
    @WithMockUser(username = CHECK_IN_USER, roles = {"GUEST"})
    public void testCheckIn_DateOfBirth() throws Exception {
        Room room = Room.RoomBuilder.aRoom()
            .withName(TEST_ROOM_NAME)
            .withDescription(TEST_ROOM_DESCRIPTION)
            .withPrice(TEST_ROOM_PRICE)
            .withCapacity(TEST_ROOM_CAPACITY)
            .withMainImage(TEST_ROOM_MAIN_IMAGE)
            .withCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime())
            .build();
        roomRepository.save(room);

        Booking CHECK_IN_BOOKING = Booking.BookingBuilder.aBooking()
            .withUser(CHECK_IN_USER_GUEST)
            .withRoom(room)
            .withStartDate(LocalDate.now())
            .withEndDate(LocalDate.now().plusDays(7))
            .build();
        Long CHECK_IN_BOOKING_ID = bookingRepository.save(CHECK_IN_BOOKING).getId();

        // Create CheckInDto with test data
        CheckInDto checkInDto = new CheckInDto(
            CHECK_IN_BOOKING_ID,
            CHECK_IN_USER_GUEST.getFirstName(),
            CHECK_IN_USER_GUEST.getLastName(),
            LocalDate.now(),  // guest has to be at least 18, will throw ValidationException
            CHECK_IN_USER_GUEST.getPlaceOfBirth(),
            CHECK_IN_USER_GUEST.getGender(),
            CHECK_IN_USER_GUEST.getNationality(),
            CHECK_IN_USER_GUEST.getAddress(),
            CHECK_IN_USER_GUEST.getPassportNumber(),
            CHECK_IN_USER_GUEST.getPhoneNumber()
        );

        // Mock passport file
        MockMultipartFile passportFile = new MockMultipartFile(
            "passport",
            "passport.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "mock-passport-content".getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, CHECK_IN_BASE_URI)
                    .file(passportFile)
                    .param("bookingId", String.valueOf(checkInDto.bookingId()))
                    .param("firstName", checkInDto.firstName())
                    .param("lastName", checkInDto.lastName())
                    .param("dateOfBirth", checkInDto.dateOfBirth().toString())
                    .param("placeOfBirth", checkInDto.placeOfBirth())
                    .param("gender", checkInDto.gender().toString())
                    .param("nationality", checkInDto.nationality().toString())
                    .param("address", checkInDto.address())
                    .param("passportNumber", checkInDto.passportNumber())
                    .param("phoneNumber", checkInDto.phoneNumber())
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Test case: Fails to check into a booking when providing malformed passport number.
     */
    @Test
    @WithMockUser(username = CHECK_IN_USER, roles = {"GUEST"})
    public void testCheckIn_PassportNumber() throws Exception {
        Room room = Room.RoomBuilder.aRoom()
            .withName(TEST_ROOM_NAME)
            .withDescription(TEST_ROOM_DESCRIPTION)
            .withPrice(TEST_ROOM_PRICE)
            .withCapacity(TEST_ROOM_CAPACITY)
            .withMainImage(TEST_ROOM_MAIN_IMAGE)
            .withCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime())
            .build();
        roomRepository.save(room);

        Booking CHECK_IN_BOOKING = Booking.BookingBuilder.aBooking()
            .withUser(CHECK_IN_USER_GUEST)
            .withRoom(room)
            .withStartDate(LocalDate.now())
            .withEndDate(LocalDate.now().plusDays(7))
            .build();
        Long CHECK_IN_BOOKING_ID = bookingRepository.save(CHECK_IN_BOOKING).getId();

        // Create CheckInDto with test data
        CheckInDto checkInDto = new CheckInDto(
            CHECK_IN_BOOKING_ID,
            CHECK_IN_USER_GUEST.getFirstName(),
            CHECK_IN_USER_GUEST.getLastName(),
            CHECK_IN_USER_GUEST.getDateOfBirth(),
            CHECK_IN_USER_GUEST.getPlaceOfBirth(),
            CHECK_IN_USER_GUEST.getGender(),
            CHECK_IN_USER_GUEST.getNationality(),
            CHECK_IN_USER_GUEST.getAddress(),
            "42",  // passport numbers should be at least 6 alphanumerical characters, will throw ValidationException
            CHECK_IN_USER_GUEST.getPhoneNumber()
        );

        // Mock passport file
        MockMultipartFile passportFile = new MockMultipartFile(
            "passport",
            "passport.pdf",
            MediaType.APPLICATION_PDF_VALUE,
            "mock-passport-content".getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, CHECK_IN_BASE_URI)
                    .file(passportFile)
                    .param("bookingId", String.valueOf(checkInDto.bookingId()))
                    .param("firstName", checkInDto.firstName())
                    .param("lastName", checkInDto.lastName())
                    .param("dateOfBirth", checkInDto.dateOfBirth().toString())
                    .param("placeOfBirth", checkInDto.placeOfBirth())
                    .param("gender", checkInDto.gender().toString())
                    .param("nationality", checkInDto.nationality().toString())
                    .param("address", checkInDto.address())
                    .param("passportNumber", checkInDto.passportNumber())
                    .param("phoneNumber", checkInDto.phoneNumber())
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity());
    }
}