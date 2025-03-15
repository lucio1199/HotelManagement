package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.BookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserLoginDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.Booking;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.entity.UiConfig;
import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
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
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleMailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import at.ac.tuwien.sepr.groupphase.backend.repository.UiConfigRepository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Slf4j
@Import(TestSecurityConfig.class)
public class BookingEndpointTest implements TestData {

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

    @MockitoBean
    private UiConfigRepository uiConfigRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @MockitoBean
    private SimpleMailService mailService;


    @BeforeEach
    public void setup() {
        checkInRepository.deleteAll();
        bookingRepository.deleteAll();
        roomRepository.deleteAll();

        BOOKING_USER_GUEST.setEmail(BOOKING_USER);
        BOOKING_USER_GUEST.setPassword(passwordEncoder.encode("password2"));
        BOOKING_USER_GUEST.setRoleType(RoleType.ROLE_GUEST);
        BOOKING_USER_GUEST.setVerified(true);
        BOOKING_USER_GUEST.setPhoneNumber("+12345");

        BOOKING_USER_GUEST.setFirstName("Test2");
        BOOKING_USER_GUEST.setLastName("User2");
        BOOKING_USER_GUEST.setDateOfBirth(LocalDate.parse("1990-01-" + String.format("%02d", 2)));
        BOOKING_USER_GUEST.setPlaceOfBirth("City2");
        BOOKING_USER_GUEST.setGender(Gender.FEMALE);
        BOOKING_USER_GUEST.setNationality(Nationality.values()[2 % Nationality.values().length]);
        BOOKING_USER_GUEST.setAddress("124 Main St, City");
        BOOKING_USER_GUEST.setPassportNumber("P1234568");
        guestRepository.save(BOOKING_USER_GUEST);


        userService.login(new UserLoginDto(BOOKING_USER_GUEST.getEmail(), "password2"));

        roomRepository.deleteAll();
    }

    /**
     * Test case: Successfully books a room.
     */
    @Test
    @WithMockUser(username = BOOKING_USER, roles = {"GUEST"})
    public void testBooking_Success() throws Exception {
        Room room = Room.RoomBuilder.aRoom()
            .withName(TEST_ROOM_NAME)
            .withDescription(TEST_ROOM_DESCRIPTION)
            .withPrice(TEST_ROOM_PRICE)
            .withCapacity(TEST_ROOM_CAPACITY)
            .withMainImage(TEST_ROOM_MAIN_IMAGE)
            .withCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime())
            .build();
        roomRepository.save(room);

        BookingCreateDto bookingCreateDto = new BookingCreateDto(
            room.getId(),
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "PayInAdvance"
        );

        String bookingJson = objectMapper.writeValueAsString(bookingCreateDto);

        UiConfig uiConfig = new UiConfig();
        uiConfig.setHotelName("InnControl Hotel");
        when(uiConfigRepository.findById(1L)).thenReturn(Optional.of(uiConfig));

        mockMvc.perform(
                MockMvcRequestBuilders.post(BOOKING_BASE_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bookingJson))
            .andExpect(status().isCreated());
    }

    /**
     * Test case: Tries to book a room that does not exist.
     */
    @Test
    @WithMockUser(username = BOOKING_USER, roles = {"GUEST"})
    public void testBooking_NonExistingRoom() throws Exception {
        BookingCreateDto bookingCreateDto = new BookingCreateDto(
            -1L,
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "PayInAdvance"
        );

        String bookingJson = objectMapper.writeValueAsString(bookingCreateDto);

        mockMvc.perform(
                MockMvcRequestBuilders.post(BOOKING_BASE_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bookingJson))
            .andExpect(status().isUnprocessableEntity());
    }

    /**
     * Test case: Successfully cancels an existing booking.
     */
    @Test
    @WithMockUser(username = BOOKING_USER, roles = {"GUEST"})
    public void testCancelBooking_Success() throws Exception {
        // Save a booking to be canceled
        Room room = Room.RoomBuilder.aRoom()
            .withName(TEST_ROOM_NAME)
            .withDescription(TEST_ROOM_DESCRIPTION)
            .withPrice(TEST_ROOM_PRICE)
            .withCapacity(TEST_ROOM_CAPACITY)
            .withMainImage(TEST_ROOM_MAIN_IMAGE)
            .withCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime())
            .build();
        roomRepository.save(room);

        BookingCreateDto bookingCreateDto = new BookingCreateDto(
            room.getId(),
            LocalDate.now(),
            LocalDate.now().plusDays(7),
            "PayInAdvance"
        );

        String bookingJson = objectMapper.writeValueAsString(bookingCreateDto);

        UiConfig uiConfig = new UiConfig();
        uiConfig.setHotelName("InnControl Hotel");
        when(uiConfigRepository.findById(1L)).thenReturn(Optional.of(uiConfig));

        mockMvc.perform(
                MockMvcRequestBuilders.post(BOOKING_BASE_URI)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(bookingJson))
            .andExpect(status().isCreated());

        Long bookingId = 1L;

        mockMvc.perform(
                MockMvcRequestBuilders.delete("/api/v1/bookings/my-bookings/{bookingId}/cancel", bookingId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(BOOKING_USER, GUEST_ROLES)))
            .andExpect(status().isOk());

        Booking canceledBooking = bookingRepository.findById(bookingId).orElseThrow();
        assertThat(canceledBooking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }


    /**
     * Test case: Attempts to get a booking by ID that does not exist.
     */
    @Test
    @WithMockUser(username = BOOKING_USER, roles = {"GUEST"})
    public void testGetBookingById_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    /**
     * Test case: Successfully retrieves all bookings as an admin.
     */
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void testGetAllBookings_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/managerbookings")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }

    /**
     * Test case: Forbids a guest from accessing all bookings.
     */
    @Test
    @WithMockUser(username = "guest", roles = {"GUEST"})
    public void testGetAllBookings_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/managerbookings")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }


    /**
     * Test case: Attempts to cancel a booking that does not exist.
     */
    @Test
    @WithMockUser(username = BOOKING_USER, roles = {"GUEST"})
    public void testCancelBooking_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/bookings/my-bookings/{bookingId}/cancel", 999L)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    /**
     * Test case: Tests for invalid tokens during the booking retrieval.
     */
    @Test
    @WithMockUser(username = TEST_USER_EMAIL, roles = {"GUEST"})
    public void testGetBookingsByUser_InvalidToken() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/v1/bookings/my-bookings")
                    .header("Authorization", "Bearer invalidToken")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    /**
     * Test case: Retrieves paginated bookings successfully as an admin.
     */
    @Test
    @WithMockUser(username = "manager", roles = {"ADMIN"})
    public void testGetPagedBookings_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/managerbookings/paged")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    /**
     * Test case: Retrieves paginated bookings with invalid parameters.
     */
    @Test
    @WithMockUser(username = "manager", roles = {"ADMIN"})
    public void testGetPagedBookings_InvalidParameters() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/managerbookings/paged")
                .param("page", "-1")
                .param("size", "-10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Page index must not be less than zero"))
            .andExpect(jsonPath("$.status").value(400));
    }


    /**
     * Test case: Successfully downloads a booking PDF.
     */
    @Test
    @WithMockUser(username = BOOKING_USER, roles = {"GUEST"})
    public void testDownloadPdf_Success() throws Exception {
        Long bookingId = 1L;
        String type = "Invoice.pdf";

        UiConfig uiConfig = new UiConfig();
        uiConfig.setHotelName("InnControl Hotel");
        when(uiConfigRepository.findById(1L)).thenReturn(Optional.of(uiConfig));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/my-bookings/{bookingId}/pdf/{type}", bookingId, type)
                .contentType(MediaType.APPLICATION_PDF))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"Invoice.pdf\""));

    }

    /**
     * Test case: Attempts to download a non-existing booking PDF.
     */
    @Test
    @WithMockUser(username = BOOKING_USER, roles = {"GUEST"})
    public void testDownloadPdf_NotFound() throws Exception {
        Long bookingId = 999L;
        String type = "nonexistent";

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/bookings/my-bookings/{bookingId}/pdf/{type}", bookingId, type)
                .contentType(MediaType.APPLICATION_PDF))
            .andExpect(status().isNotFound());
    }
}