package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.UiConfigMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.UiConfig;
import at.ac.tuwien.sepr.groupphase.backend.repository.UiConfigRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class UiConfigEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UiConfigRepository uiConfigRepository;


    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    SecurityProperties securityProperties;

    @Autowired
    @Qualifier("userDetailsServiceStub")
    private UserDetailsService userDetailsServiceStub;

    private UiConfig uiConfig;


    @BeforeEach
    public void beforeEach() {
        uiConfigRepository.deleteAll();
        uiConfig = uiConfigRepository.save(new UiConfig(
            TEST_UI_CONFIG_ID,
            TEST_UI_CONFIG_HOTEL_NAME,
            TEST_UI_CONFIG_DESCRIPTION_SHORT,
            TEST_UI_CONFIG_DESCRIPTION,
            TEST_UI_CONFIG_ADDRESS,
            TEST_UI_CONFIG_ROOM_CLEANING,
            TEST_UI_CONFIG_DIGITAL_CHECKIN,
            TEST_UI_CONFIG_ACTIVITIES,
            TEST_UI_CONFIG_COMMUNICATION,
            TEST_UI_CONFIG_NUKI,
            TEST_UI_CONFIG_HALF_BOARD,
            TEST_UI_CONFIG_PRICE_HALF_BOARD,
            TEST_UI_CONFIG_IMAGES
        ));
    }


    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateUiConfig_Success() throws Exception {

        mockMvc.perform(
                multipart(HttpMethod.PUT, UI_CONFIG_BASE_URI + "/{id}", uiConfig.getId())
                    .param("id", String.valueOf(uiConfig.getId()))
                    .param("hotelName", "Updated Hotel Name")
                    .param("descriptionShort", "Updated Description Short")
                    .param("description", "Updated Description")
                    .param("roomCleaning", "true")
                    .param("digitalCheckIn", "false")
                    .param("activities", "true")
                    .param("communication", "true")
                    .param("nuki", "true")
                    .param("halfBoard", "false")
                    .param("priceHalfBoard", "0")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hotelName").value("Updated Hotel Name"))
            .andExpect(jsonPath("$.descriptionShort").value("Updated Description Short"))
            .andExpect(jsonPath("$.description").value("Updated Description"))
            .andExpect(jsonPath("$.roomCleaning").value(true))
            .andExpect(jsonPath("$.digitalCheckIn").value(false))
            .andExpect(jsonPath("$.activities").value(true))
            .andExpect(jsonPath("$.communication").value(true))
            .andExpect(jsonPath("$.nuki").value(true))
            .andExpect(jsonPath("$.halfBoard").value(false))
            .andExpect(jsonPath("$.priceHalfBoard").value(0));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateUiConfig_InvalidHotelNameTooShort_Failure() throws Exception {
        mockMvc.perform(
                multipart(HttpMethod.PUT, UI_CONFIG_BASE_URI + "/{id}", uiConfig.getId())
                    .param("id", String.valueOf(uiConfig.getId()))
                    .param("hotelName", "Hi")
                    .param("descriptionShort", "Updated Description Short")
                    .param("description", "Valid Description")
                    .param("roomCleaning", "true")
                    .param("digitalCheckIn", "true")
                    .param("activities", "true")
                    .param("communication", "true")
                    .param("nuki", "true")
                    .param("halfBoard", "true")
                    .param("priceHalfBoard", "0")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Hotel name must be between 3 and 100 characters."));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateUiConfig_HotelNameTooLong_Failure() throws Exception {
        String longHotelName = "a".repeat(101); // Exceeds max length

        mockMvc.perform(
                multipart(HttpMethod.PUT, UI_CONFIG_BASE_URI + "/{id}", uiConfig.getId())
                    .param("id", String.valueOf(uiConfig.getId()))
                    .param("hotelName", longHotelName)
                    .param("descriptionShort", "Updated Description Short")
                    .param("description", "Valid Description")
                    .param("roomCleaning", "true")
                    .param("digitalCheckIn", "true")
                    .param("activities", "true")
                    .param("communication", "true")
                    .param("nuki", "true")
                    .param("halfBoard", "true")
                    .param("priceHalfBoard", "0")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Hotel name must be between 3 and 100 characters."));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateUiConfig_DescriptionTooLong_Failure() throws Exception {
        String longDescription = "a".repeat(1001); // Exceeds max length

        mockMvc.perform(
                multipart(HttpMethod.PUT, UI_CONFIG_BASE_URI + "/{id}", uiConfig.getId())
                    .param("id", String.valueOf(uiConfig.getId()))
                    .param("hotelName", "Valid Hotel Name")
                    .param("descriptionShort", "Updated Description Short")
                    .param("description", longDescription)
                    .param("roomCleaning", "true")
                    .param("digitalCheckIn", "true")
                    .param("activities", "true")
                    .param("communication", "true")
                    .param("nuki", "true")
                    .param("halfBoard", "true")
                    .param("priceHalfBoard", "0")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Description must be between 3 and 1000 characters."));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateUiConfig_InvalidImageType_Failure() throws Exception {

        MockMultipartFile invalidImage = new MockMultipartFile(
            "images",
            "image.txt",
            "text/plain", // Invalid type
            "Invalid content".getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.PUT, UI_CONFIG_BASE_URI + "/{id}", uiConfig.getId())
                    .file(invalidImage)
                    .param("id", String.valueOf(uiConfig.getId()))
                    .param("hotelName", "Valid Hotel Name")
                    .param("descriptionShort", "Updated Description Short")
                    .param("description", "Valid Description")
                    .param("roomCleaning", "true")
                    .param("digitalCheckIn", "true")
                    .param("activities", "true")
                    .param("communication", "true")
                    .param("nuki", "true")
                    .param("halfBoard", "true")
                    .param("priceHalfBoard", "0")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Invalid image type. Allowed types are: JPEG, PNG."));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateUiConfig_ImageSizeTooLarge_Failure() throws Exception {

        byte[] oversizedImage = new byte[1024 * 1024 + 1]; // Exceeds 1 MB

        MockMultipartFile largeImage = new MockMultipartFile(
            "images",
            "large_image.jpg",
            "image/jpeg",
            oversizedImage
        );

        mockMvc.perform(
                multipart(HttpMethod.PUT, UI_CONFIG_BASE_URI + "/{id}", uiConfig.getId())
                    .file(largeImage)
                    .param("id", String.valueOf(uiConfig.getId()))
                    .param("hotelName", "Valid Hotel Name")
                    .param("descriptionShort", "Updated Description Short")
                    .param("description", "Valid Description")
                    .param("roomCleaning", "true")
                    .param("digitalCheckIn", "true")
                    .param("activities", "true")
                    .param("communication", "true")
                    .param("nuki", "true")
                    .param("halfBoard", "true")
                    .param("priceHalfBoard", "0")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Image size exceeds the maximum limit of 1 MB."));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateUiConfig_ValidHotelName_MaxLength_Success() throws Exception {
        String maxHotelName = "a".repeat(100); // Maximum allowed length

        mockMvc.perform(
                multipart(HttpMethod.PUT, UI_CONFIG_BASE_URI + "/{id}", uiConfig.getId())
                    .param("id", String.valueOf(uiConfig.getId()))
                    .param("hotelName", maxHotelName)
                    .param("descriptionShort", "Updated Description Short")
                    .param("description", "Valid Description")
                    .param("roomCleaning", "true")
                    .param("digitalCheckIn", "true")
                    .param("activities", "true")
                    .param("communication", "true")
                    .param("nuki", "true")
                    .param("halfBoard", "true")
                    .param("priceHalfBoard", "0")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hotelName").value(maxHotelName))
            .andExpect(jsonPath("$.descriptionShort").value("Updated Description Short"))
            .andExpect(jsonPath("$.description").value("Valid Description"))
            .andExpect(jsonPath("$.roomCleaning").value(true))
            .andExpect(jsonPath("$.digitalCheckIn").value(true))
            .andExpect(jsonPath("$.activities").value(true))
            .andExpect(jsonPath("$.communication").value(true))
            .andExpect(jsonPath("$.nuki").value(true))
            .andExpect(jsonPath("$.halfBoard").value(true))
            .andExpect(jsonPath("$.priceHalfBoard").value(0));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateUiConfig_ValidDescription_MaxLength_Success() throws Exception {
        String maxDescription = "a".repeat(1000); // Maximum allowed length

        mockMvc.perform(
                multipart(HttpMethod.PUT, UI_CONFIG_BASE_URI + "/{id}", uiConfig.getId())
                    .param("id", String.valueOf(uiConfig.getId()))
                    .param("hotelName", "Valid Hotel Name")
                    .param("descriptionShort", "Updated Description Short")
                    .param("description", maxDescription)
                    .param("roomCleaning", "true")
                    .param("digitalCheckIn", "true")
                    .param("activities", "true")
                    .param("communication", "true")
                    .param("nuki", "true")
                    .param("halfBoard", "true")
                    .param("priceHalfBoard", "0")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hotelName").value("Valid Hotel Name"))
            .andExpect(jsonPath("$.descriptionShort").value("Updated Description Short"))
            .andExpect(jsonPath("$.description").value(maxDescription))
            .andExpect(jsonPath("$.roomCleaning").value(true))
            .andExpect(jsonPath("$.digitalCheckIn").value(true))
            .andExpect(jsonPath("$.activities").value(true))
            .andExpect(jsonPath("$.communication").value(true))
            .andExpect(jsonPath("$.nuki").value(true))
            .andExpect(jsonPath("$.halfBoard").value(true))
            .andExpect(jsonPath("$.priceHalfBoard").value(0));
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateUiConfig_ValidImage_Success() throws Exception {
        MockMultipartFile validImage = new MockMultipartFile(
            "images",
            "valid_image.jpg",
            "image/jpeg",
            "Valid Image Content".getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.PUT, UI_CONFIG_BASE_URI + "/{id}", uiConfig.getId())
                    .file(validImage)
                    .param("id", String.valueOf(uiConfig.getId()))
                    .param("hotelName", "Valid Hotel Name")
                    .param("descriptionShort", "Updated Description Short")
                    .param("description", "Valid Description")
                    .param("roomCleaning", "true")
                    .param("digitalCheckIn", "true")
                    .param("activities", "true")
                    .param("communication", "true")
                    .param("nuki", "true")
                    .param("halfBoard", "true")
                    .param("priceHalfBoard", "0")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hotelName").value("Valid Hotel Name"))
            .andExpect(jsonPath("$.descriptionShort").value("Updated Description Short"))
            .andExpect(jsonPath("$.description").value("Valid Description"))
            .andExpect(jsonPath("$.roomCleaning").value(true))
            .andExpect(jsonPath("$.digitalCheckIn").value(true))
            .andExpect(jsonPath("$.activities").value(true))
            .andExpect(jsonPath("$.communication").value(true))
            .andExpect(jsonPath("$.nuki").value(true))
            .andExpect(jsonPath("$.halfBoard").value(true))
            .andExpect(jsonPath("$.priceHalfBoard").value(0))
            .andExpect(jsonPath("$.images[0]").isNotEmpty());
    }

    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateUiConfig_AllValidFields_Success() throws Exception {
        MockMultipartFile validImage = new MockMultipartFile(
            "images",
            "valid_image.jpg",
            "image/jpeg",
            "Valid Image Content".getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.PUT, UI_CONFIG_BASE_URI + "/{id}", uiConfig.getId())
                    .file(validImage)
                    .param("id", String.valueOf(uiConfig.getId()))
                    .param("hotelName", "Valid Hotel Name")
                    .param("descriptionShort", "Updated Description Short")
                    .param("description", "Valid Description")
                    .param("roomCleaning", "true")
                    .param("digitalCheckIn", "true")
                    .param("activities", "true")
                    .param("communication", "true")
                    .param("nuki", "true")
                    .param("halfBoard", "true")
                    .param("priceHalfBoard", "0")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hotelName").value("Valid Hotel Name"))
            .andExpect(jsonPath("$.descriptionShort").value("Updated Description Short"))
            .andExpect(jsonPath("$.description").value("Valid Description"))
            .andExpect(jsonPath("$.roomCleaning").value(true))
            .andExpect(jsonPath("$.digitalCheckIn").value(true))
            .andExpect(jsonPath("$.activities").value(true))
            .andExpect(jsonPath("$.communication").value(true))
            .andExpect(jsonPath("$.nuki").value(true))
            .andExpect(jsonPath("$.halfBoard").value(true))
            .andExpect(jsonPath("$.priceHalfBoard").value(0))
            .andExpect(jsonPath("$.images[0]").isNotEmpty());
    }


    @Test
    public void testGetHomepageConfig_Success() throws Exception {
        mockMvc.perform(request(HttpMethod.GET, UI_CONFIG_BASE_URI + "/homepage")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hotelName").value(TEST_UI_CONFIG_HOTEL_NAME))
            .andExpect(jsonPath("$.descriptionShort").value(TEST_UI_CONFIG_DESCRIPTION_SHORT))
            .andExpect(jsonPath("$.description").value(TEST_UI_CONFIG_DESCRIPTION))
            .andExpect(jsonPath("$.address").value(TEST_UI_CONFIG_ADDRESS))
            .andExpect(jsonPath("$.images").isArray());
    }

    @Test
    public void testGetHomepageConfig_NoConfigFound() throws Exception {
        uiConfigRepository.deleteAll();

        mockMvc.perform(request(HttpMethod.GET, UI_CONFIG_BASE_URI + "/homepage")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}