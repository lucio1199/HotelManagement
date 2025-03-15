package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityBooking;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivitySlot;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.enums.BookingStatus;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityBookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivitySlotRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.ActivityBookingService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@Import(TestSecurityConfig.class)
public class ActivityBookingEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActivityBookingService activityBookingService;

    @Autowired
    private ActivityBookingRepository activityBookingRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ActivitySlotRepository activitySlotRepository;

    @Autowired
    private ActivityRepository activityRepository;

    ApplicationUser testUser;

    Activity testActivity;

    ActivitySlot testSlot;

    @Autowired
    private UserService userService;

    @BeforeEach
    public void setUp() {
        activityBookingRepository.deleteAll();
        activitySlotRepository.deleteAll();
        activityRepository.deleteAll();
        applicationUserRepository.deleteAll();

        testUser = new ApplicationUser();
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");
        testUser = applicationUserRepository.save(testUser);

        testActivity = new Activity();
        testActivity.setName("Test Activity");
        testActivity.setDescription("Test Description");
        testActivity.setPrice(50.0);
        testActivity.setCapacity(10);
        testActivity.setCategories("");

        testSlot = new ActivitySlot();
        testSlot.setActivity(testActivity);
        testSlot.setStartTime(LocalTime.of(12, 0));
        testSlot.setEndTime(LocalTime.of(13, 0));
        testSlot.setDate(LocalDate.now());
        testSlot.setOccupied(0);
        testSlot.setCapacity(10);
        testActivity.setActivityTimeslots(new ArrayList<>(List.of(testSlot)));

        testActivity = activityRepository.save(testActivity);
    }

    @Test
    @Disabled
    @WithMockUser(username = "testuser@example.com", roles = {"GUEST"})
    public void createBooking_Success() throws Exception {
        ActivityBookingCreateDto bookingCreateDto = new ActivityBookingCreateDto(
            testActivity.getId(),
            testActivity.getActivityTimeslots().get(0).getId(),
            LocalDate.now(),
            3,
            "testuser@example.com"
        );

        mockMvc.perform(post("/api/v1/activity-booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingCreateDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.activityName").value("Test Activity"))
            .andExpect(jsonPath("$.participants").value(3))
            .andExpect(jsonPath("$.paid").value(false));

        ActivityBooking savedBooking = activityBookingRepository.findAll().get(0);
        assert savedBooking.getParticipants() == 3;
        assert savedBooking.getActivity().getName().equals("Test Activity");
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"GUEST"})
    public void getBookingsByUser_Success() throws Exception {

        ActivityBooking booking = new ActivityBooking();
        booking.setUser(testUser);
        booking.setActivity(testActivity);
        booking.setActivitySlot(testActivity.getActivityTimeslots().get(0));
        booking.setBookingDate(LocalDate.now());
        booking.setParticipants(3);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setPaid(false);


        activityBookingRepository.save(booking);

        mockMvc.perform(get("/api/v1/activity-booking/my-bookings")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].activityName").value("Test Activity"))
            .andExpect(jsonPath("$[0].participants").value(3))
            .andExpect(jsonPath("$[0].paid").value(false));
    }



    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"GUEST"})
    public void createBooking_InvalidActivityId() throws Exception {
        ActivityBookingCreateDto bookingCreateDto = new ActivityBookingCreateDto(
            -1L,
            testSlot.getId(),
            LocalDate.now(),
            3,
            testUser.getEmail()
        );

        mockMvc.perform(post("/api/v1/activity-booking")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingCreateDto)))
            .andExpect(status().isNotFound());
    }

    @Test
    @Disabled
    @WithMockUser(username = "testuser@example.com", roles = {"GUEST"})
    public void getBookingsByUser_NoBookings() throws Exception {
        mockMvc.perform(get("/api/v1/activity-booking/my-bookings")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser@example.com", roles = {"GUEST"})
    public void markAsPaid_Success() throws Exception {


        ActivityBooking booking = new ActivityBooking();
        booking.setUser(testUser);
        booking.setActivity(testActivity);
        booking.setActivitySlot(testActivity.getActivityTimeslots().get(0));
        booking.setBookingDate(LocalDate.now());
        booking.setParticipants(3);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setPaid(false);


        activityBookingRepository.save(booking);
        mockMvc.perform(put("/api/v1/activity-booking/{bookingId}", booking.getId()))
            .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = {"GUEST"})
    public void markAsPaid_BookingNotFound() throws Exception {

        // Perform the PUT request
        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/activity-booking/{bookingId}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Booking not found with ID: 999"));

    }
}
