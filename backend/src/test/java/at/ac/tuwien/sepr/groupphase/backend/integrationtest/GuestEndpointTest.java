package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepr.groupphase.backend.entity.Guest;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@Import(TestSecurityConfig.class)
public class GuestEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    private BookingRepository bookingRepository;

    private Guest guest;


    @BeforeEach
    public void setup() {
        bookingRepository.deleteAll();
        guestRepository.deleteAll();
        guest = new Guest();
        guest.setEmail(TEST_GUEST_EMAIL);
        guest.setFirstName(TEST_GUEST_FIRST_NAME);
        guest.setLastName(TEST_GUEST_LAST_NAME);
        guest.setPhoneNumber(TEST_GUEST_PHONE);
        guest.setPassword("Password@123");
        guestRepository.save(guest);
    }

    @Test
    @Transactional
    public void testSignupGuest_Success() throws Exception {
        GuestSignupDto guestSignupDto = new GuestSignupDto(
            "newguest@test.com",
            "Password@123"
        );

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/guest/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guestSignupDto)))
            .andExpect(status().isCreated())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        SimpleGuestDto simpleGuestDto = objectMapper.readValue(response.getContentAsString(), SimpleGuestDto.class);

        // Assert that the email is correctly saved and password is encoded
        assertAll(
            () -> assertEquals(guestSignupDto.email(), simpleGuestDto.email()),
            () -> assertTrue(guestRepository.existsByEmail(guestSignupDto.email())),
            () -> assertTrue(passwordEncoder.matches("Password@123",
                guestRepository.findByEmail(guestSignupDto.email()).get().getPassword()))
        );
    }



    @Test
    @Transactional
    @Disabled
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testFindAllGuests() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/guest")
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isOk())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        List<GuestListDto> guestList = objectMapper.readValue(response.getContentAsString(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, GuestListDto.class));

        assertEquals(1, guestList.size());
        assertEquals(TEST_GUEST_EMAIL, guestList.get(0).email());
    }

    @Test
    @Transactional
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "GUEST"})
    public void testFindGuestByEmail_Success() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/guest/{email}", TEST_GUEST_EMAIL)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isOk())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        GuestDetailDto guestDetailDto = objectMapper.readValue(response.getContentAsString(), GuestDetailDto.class);

        assertAll(
            () -> assertEquals(TEST_GUEST_EMAIL, guestDetailDto.email()),
            () -> assertEquals(TEST_GUEST_FIRST_NAME, guestDetailDto.firstName()),
            () -> assertEquals(TEST_GUEST_LAST_NAME, guestDetailDto.lastName())
        );
    }

    @Test
    @Transactional
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testUpdateGuest_Success() throws Exception {
        GuestCreateUpdateDto guestUpdateDto = new GuestCreateUpdateDto(
            "UpdatedFirstName",
            "UpdatedLastName",
            TEST_GUEST_EMAIL,
            null,
            "Updated City",
            "MALE",
            "DEU",
            "Updated Address",
            "A1234567",
            "987654321",
            "NewPassword@123"
        );

        mockMvc.perform(put("/api/v1/guest/{email}", TEST_GUEST_EMAIL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guestUpdateDto))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.firstName").value("UpdatedFirstName"))
            .andExpect(jsonPath("$.lastName").value("UpdatedLastName"))
            .andExpect(jsonPath("$.address").value("Updated Address"));
    }

    @Test
    @Transactional
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testCreateGuest_Success() throws Exception {
        GuestCreateUpdateDto guestCreateDto = new GuestCreateUpdateDto(
            "New",
            "Guest",
            "newguest@test.com",
            LocalDate.of(1990, 1, 1),
            "City",
            "FEMALE",
            "DEU",
            "Address",
            "P9876543",
            "123456789",
            "StrongPassword@123"
        );

        mockMvc.perform(post("/api/v1/guest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guestCreateDto))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.firstName").value("New"))
            .andExpect(jsonPath("$.lastName").value("Guest"))
            .andExpect(jsonPath("$.email").value("newguest@test.com"));

        Guest createdGuest = guestRepository.findByEmail("newguest@test.com").orElseThrow();
        assertTrue(passwordEncoder.matches("StrongPassword@123", createdGuest.getPassword()));
    }

    @Test
    @Transactional
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testDeleteGuest_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/guest/{email}", TEST_GUEST_EMAIL)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isNoContent());

        assertFalse(guestRepository.existsByEmail(TEST_GUEST_EMAIL));
    }

    @Test
    @Transactional
    @Disabled
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testSearchGuests() throws Exception {
        GuestSearchDto searchDto = new GuestSearchDto("Test", null, null);

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/guest/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(searchDto))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isOk())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        List<GuestListDto> searchResults = objectMapper.readValue(response.getContentAsString(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, GuestListDto.class));

        assertFalse(searchResults.isEmpty());
    }

    @Test
    @Transactional
    public void testSignupGuestWithMissingEmail_shouldThrow() throws Exception {
        GuestSignupDto guestSignupDto = new GuestSignupDto(
            null, // Missing email
            "Password@123"
        );

        mockMvc.perform(post("/api/v1/guest/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guestSignupDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Email must not be null."));
    }

    @Transactional
    @Test
    public void testSignupGuestWithInvalidPassword_shouldThrow() throws Exception {
        GuestSignupDto guestSignupDto = new GuestSignupDto(
            "invalidpassword@test.com",
            "short" // Invalid password
        );

        mockMvc.perform(post("/api/v1/guest/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guestSignupDto)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Password must be at least 8 characters long."));
    }

    @Test
    @Transactional
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testCreateGuestWithExistingEmail_shouldThrow() throws Exception {
        GuestCreateUpdateDto guestCreateDto = new GuestCreateUpdateDto(
            "Duplicate",
            "Email",
            TEST_GUEST_EMAIL,
            LocalDate.of(1990, 1, 1),
            "City",
            "FEMALE",
            "DEU",
            "Address",
            "P9876543",
            "123456789",
            "StrongPassword@123"
        );

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/guest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guestCreateDto))
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andExpect(status().isConflict())
            .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals("application/json", response.getContentType());

        String responseBody = response.getContentAsString();
        assertAll(
            () -> assertTrue(responseBody.contains("Email conflict")),
            () -> assertTrue(responseBody.contains("Email already exists"))
        );
    }
}
