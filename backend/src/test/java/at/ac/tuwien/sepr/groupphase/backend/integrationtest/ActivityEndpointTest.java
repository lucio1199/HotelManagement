package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ActivityMapper;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.RoomMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivitySlot;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityTimeslotInfo;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import at.ac.tuwien.sepr.groupphase.backend.service.ActivityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class ActivityEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ActivityRepository activityRepository;


    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    @Qualifier("userDetailsServiceStub")
    private UserDetailsService userDetailsServiceStub;

    private Activity activity = new Activity(
        TEST_ACTIVITY_ID,
        TEST_ACTIVITY_NAME,
        TEST_ACTIVITY_DESCRIPTION,
        TEST_ACTIVITY_PRICE,
        TEST_ACTIVITY_CAPACITY,
        TEST_ACTIVITY_ADDITIONAL_IMAGES_AS_ACTIVITYIMAGE,
        null,
        TEST_ACTIVITY_TIMESLOTS1,
        TEST_ACTIVITY_MAIN_IMAGE,
        TEST_ACTIVITY_CATEGORIES);

    /**
     * Setup method to prepare the test environment before each test.
     * Clears the Room repository and prepares a default room object.
     */
    @BeforeEach
    public void beforeEach() {
        activityRepository.deleteAll();
        // Initialisiere die Timeslots und speichere sie einzeln
        ActivityTimeslotInfo timeslot1 = new ActivityTimeslotInfo();
        timeslot1.setDayOfWeek(DayOfWeek.MONDAY);
        timeslot1.setStartTime(LocalTime.of(10, 0));
        timeslot1.setEndTime(LocalTime.of(12, 0));

        ActivityTimeslotInfo timeslot2 = new ActivityTimeslotInfo();
        timeslot2.setDayOfWeek(DayOfWeek.WEDNESDAY);
        timeslot2.setStartTime(LocalTime.of(12, 0));
        timeslot2.setEndTime(LocalTime.of(15, 0));

        List<ActivityTimeslotInfo> managedTimeslots = new ArrayList<>();
        managedTimeslots.add(timeslot1);
        managedTimeslots.add(timeslot2);
        activity = Activity.ActivityBuilder.aActivity()
            .withName(TEST_ACTIVITY_NAME)
            .withDescription(TEST_ACTIVITY_DESCRIPTION)
            .withPrice(TEST_ACTIVITY_PRICE)
            .withCapacity(TEST_ACTIVITY_CAPACITY)
            .withMainImage(TEST_ACTIVITY_MAIN_IMAGE)
            .withTimeslotInfos(managedTimeslots)
            .withCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime())
            .withCategories(TEST_ACTIVITY_CATEGORIES)
            .build();
        activityRepository.save(activity);
    }

    private List<ActivitySlot> generateAndSaveActivitySlots(Activity activity) {
        ActivitySlot slot1 = new ActivitySlot(
            null,
            activity,
            LocalDate.now().plusDays(1),
            LocalTime.of(10, 0),
            LocalTime.of(12, 0),
            15,
            5
        );

        ActivitySlot slot2 = new ActivitySlot(
            null,
            activity,
            LocalDate.now().plusDays(2),
            LocalTime.of(14, 0),
            LocalTime.of(16, 0),
            15,
            8
        );

        return List.of(slot1, slot2);
    }


    /**
     * Test case: Successfully creates a new activity.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateActivity_Success() throws Exception {
        ActivityCreateDto activityCreateDto = new ActivityCreateDto(
            TEST_ACTIVITY_NAME,
            TEST_ACTIVITY_DESCRIPTION,
            TEST_ACTIVITY_CAPACITY,
            TEST_ACTIVITY_PRICE,
            TEST_ACTIVITY_CATEGORIES
            );

        MockMultipartFile mainImage = new MockMultipartFile(
            "mainImage",
            "testImage.jpg",
            "image/jpeg",
            TEST_ACTIVITY_MAIN_IMAGE
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, ACTIVITY_BASE_URI)
                    .file(mainImage)
                    .param("name", activityCreateDto.name())
                    .param("description", activityCreateDto.description())
                    .param("capacity", String.valueOf(activityCreateDto.capacity()))
                    .param("price", String.valueOf(activityCreateDto.price()))
                    .param("categories", String.valueOf(activityCreateDto.categories()))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(TEST_ACTIVITY_NAME))
            .andExpect(jsonPath("$.description").value(TEST_ACTIVITY_DESCRIPTION))
            .andExpect(jsonPath("$.capacity").value(TEST_ACTIVITY_CAPACITY))
            .andExpect(jsonPath("$.price").value(TEST_ACTIVITY_PRICE))
            .andExpect(jsonPath("$.categories").value(TEST_ACTIVITY_CATEGORIES));
    }

    /**
     * Test case: Fails when activity name is missing.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateActivity_MissingName() throws Exception {
        ActivityCreateDto activityCreateDto = new ActivityCreateDto(
            null,
            TEST_ACTIVITY_DESCRIPTION,
            TEST_ACTIVITY_CAPACITY,
            TEST_ACTIVITY_PRICE,
            TEST_ACTIVITY_CATEGORIES
        );

        MockMultipartFile mainImage = new MockMultipartFile(
            "mainImage",
            "testImage.jpg",
            "image/jpeg",
            TEST_ACTIVITY_MAIN_IMAGE
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, ACTIVITY_BASE_URI)
                    .file(mainImage)
                    .param("description", activityCreateDto.description())
                    .param("capacity", String.valueOf(activityCreateDto.capacity()))
                    .param("price", String.valueOf(activityCreateDto.price()))
                    .param("categories", String.valueOf(activityCreateDto.categories()))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Name must not be null."));
    }

    /**
     * Test case: Successfully updates an existing activity.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateActivity_Success() throws Exception {
        activityRepository.save(activity);
        mockMvc.perform(
                multipart(HttpMethod.PUT, ACTIVITY_BASE_URI + "/{id}", activity.getId())
                    .param("id", String.valueOf(activity.getId()))
                    .param("name", "Updated Activity Name")
                    .param("description", "Updated Activity Description")
                    .param("price", String.valueOf(350.0))
                    .param("capacity", String.valueOf(3))
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Activity Name"))
            .andExpect(jsonPath("$.description").value("Updated Activity Description"))
            .andExpect(jsonPath("$.price").value(350.0))
            .andExpect(jsonPath("$.capacity").value(3));
    }
    /**
     * Test case: Successfully deletes an existing activity.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testDeleteActivity_Success() throws Exception {
        activityRepository.save(activity);

        mockMvc.perform(
                multipart(HttpMethod.DELETE, ACTIVITY_BASE_URI + "/activities/{id}", activity.getId())
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk());
    }

    /**
     * Test case: Ensures validation fails when price is negative.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateActivity_InvalidPrice() throws Exception {
        activityRepository.save(activity);

        mockMvc.perform(
                multipart(HttpMethod.PUT, ACTIVITY_BASE_URI + "/{id}", activity.getId())
                    .param("id", String.valueOf(activity.getId()))
                    .param("name", "Valid Name")
                    .param("description", "Valid Description")
                    .param("price", String.valueOf(-1.0))
                    .param("capacity", String.valueOf(2))
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Activity price must be a positive number."));
    }

    /**
     * Test case: Fails to delete a non-existent activity.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testDeleteActivity_NonExisting() throws Exception {
        mockMvc.perform(
                multipart(HttpMethod.DELETE, ACTIVITY_BASE_URI + "/activities/{id}", -1) // Simulating request for a non-existing ID
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testFindAllActivities_Success() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get(ACTIVITY_BASE_URI + "/all")
                .param("pageIndex", "0")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @WithMockUser(username = "GUEST_USER", roles = {"GUEST"})
    public void testSearchActivities_InvalidCriteria() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(ACTIVITY_BASE_URI + "/search")
                .param("pageIndex", "-1")
                .param("pageSize", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

}
