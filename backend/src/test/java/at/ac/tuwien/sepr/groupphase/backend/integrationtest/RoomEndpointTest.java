package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.basetest.TestData;
import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedRoomDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCleaningTimeDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.RoomUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.RoomMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.CheckInRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;
import at.ac.tuwien.sepr.groupphase.backend.security.JwtTokenizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test for the Room endpoint, testing various CRUD operations
 * related to room management, including retrieval and creation of room data.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public class RoomEndpointTest implements TestData {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private JwtTokenizer jwtTokenizer;

    @Autowired
    private SecurityProperties securityProperties;

    @Autowired
    @Qualifier("userDetailsServiceStub")
    private UserDetailsService userDetailsServiceStub;

    private Room room = new Room(
        TEST_ROOM_ID,
        TEST_ROOM_NAME,
        TEST_ROOM_DESCRIPTION,
        TEST_ROOM_PRICE,
        TEST_ROOM_CAPACITY,
        TEST_ROOM_HALF_BOARD,
        TEST_ROOM_ADDITIONAL_IMAGES_AS_ROOMIMAGE,
        TEST_ROOM_LAST_CLEANED_AT,
        TEST_ROOM_CREATED_AT,
        null,
        null,
        TEST_ROOM_MAIN_IMAGE);


    /**
     * Setup method to prepare the test environment before each test.
     * Clears the Room repository and prepares a default room object.
     */
    @BeforeEach
    public void beforeEach() {
        checkInRepository.deleteAll();
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        room = Room.RoomBuilder.aRoom()
            .withName(TEST_ROOM_NAME)
            .withDescription(TEST_ROOM_DESCRIPTION)
            .withPrice(TEST_ROOM_PRICE)
            .withCapacity(TEST_ROOM_CAPACITY)
            .withMainImage(TEST_ROOM_MAIN_IMAGE)
            .withCreatedAt(new Timestamp(System.currentTimeMillis()).toLocalDateTime())
            .build();
    }


    /**
     * Test case: When a room is found by its ID, the endpoint should return the full room details.
     */
    @Test
    @Transactional
    public void givenOneRoom_whenFindById_thenRoomWithAllProperties() throws Exception {
        roomRepository.save(room);

        MvcResult mvcResult = this.mockMvc.perform(get(ROOM_BASE_URI + "/{id}", room.getId())
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();

        assertAll(
            () -> assertEquals(HttpStatus.OK.value(), response.getStatus()),
            () -> assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType())
        );

        DetailedRoomDto detailedRoomDto = objectMapper.readValue(response.getContentAsString(),
            DetailedRoomDto.class);

        Room mappedRoom = roomMapper.detailedRoomDtoToRoom(detailedRoomDto);

        assertAll(
            () -> assertEquals(room.getId(), mappedRoom.getId()),
            () -> assertEquals(room.getName(), mappedRoom.getName()),
            () -> assertEquals(room.getDescription(), mappedRoom.getDescription()),
            () -> assertEquals(room.getPrice(), mappedRoom.getPrice()),
            () -> assertEquals(room.getCapacity(), mappedRoom.getCapacity()),
            () -> assertArrayEquals(room.getMainImage(), mappedRoom.getMainImage())
        );
    }


    /**
     * Test case: When a room is not found by a non-existing ID, the endpoint should return a 404 error.
     */
    @Test
    public void givenOneRoom_whenFindByNonExistingId_then404() throws Exception {
        roomRepository.save(room);

        MvcResult mvcResult = this.mockMvc.perform(get(ROOM_BASE_URI + "/{id}", -1)
                .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES)))
            .andDo(print())
            .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    /**
     * Test case: Successfully creates a new room.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateRoom_Success() throws Exception {
        RoomCreateDto roomCreateDto = new RoomCreateDto(
            TEST_ROOM_NAME,
            TEST_ROOM_DESCRIPTION,
            TEST_ROOM_PRICE,
            TEST_ROOM_CAPACITY,
            null
        );

        MockMultipartFile mainImage = new MockMultipartFile(
            "mainImage",
            "testImage.jpg",
            "image/jpeg",
            TEST_ROOM_MAIN_IMAGE
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, ROOM_BASE_URI)
                    .file(mainImage)
                    .param("name", roomCreateDto.name())
                    .param("description", roomCreateDto.description())
                    .param("capacity", String.valueOf(roomCreateDto.capacity()))
                    .param("price", String.valueOf(roomCreateDto.price()))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(TEST_ROOM_NAME))
            .andExpect(jsonPath("$.description").value(TEST_ROOM_DESCRIPTION))
            .andExpect(jsonPath("$.capacity").value(TEST_ROOM_CAPACITY))
            .andExpect(jsonPath("$.price").value(TEST_ROOM_PRICE));
    }


    /**
     * Test case: Fails when room name is missing.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateRoom_MissingName() throws Exception {
        RoomCreateDto roomCreateDto = new RoomCreateDto(
            null,
            TEST_ROOM_DESCRIPTION,
            TEST_ROOM_PRICE,
            TEST_ROOM_CAPACITY,
            null
        );

        MockMultipartFile mainImage = new MockMultipartFile(
            "mainImage",
            "testImage.jpg",
            "image/jpeg",
            TEST_ROOM_MAIN_IMAGE
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, ROOM_BASE_URI)
                    .file(mainImage)
                    .param("description", roomCreateDto.description())
                    .param("capacity", String.valueOf(roomCreateDto.capacity()))
                    .param("price", String.valueOf(roomCreateDto.price()))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Name must not be null."));
    }


    /**
     * Tests the creation of a room when the price is missing.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateRoom_MissingPrice() throws Exception {
        RoomCreateDto roomCreateDto = new RoomCreateDto(
            TEST_ROOM_NAME,
            TEST_ROOM_DESCRIPTION,
            null,
            TEST_ROOM_CAPACITY,
            null
        );

        MockMultipartFile mainImage = new MockMultipartFile(
            "mainImage",
            "testImage.jpg",
            "image/jpeg",
            TEST_ROOM_MAIN_IMAGE
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, ROOM_BASE_URI)
                    .file(mainImage)
                    .param("name", roomCreateDto.name())
                    .param("description", roomCreateDto.description())
                    .param("capacity", String.valueOf(roomCreateDto.capacity()))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Price must not be null."));
    }


    /**
     * Tests the creation of a room with an invalid (negative) price.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateRoom_InvalidPrice() throws Exception {
        RoomCreateDto roomCreateDto = new RoomCreateDto(
            "Valid Room Name",
            "Valid Room Description",
            -100.00,
            2,
            null
        );

        MockMultipartFile mainImage = new MockMultipartFile(
            "mainImage",
            "testImage.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            TEST_ROOM_MAIN_IMAGE
        );

        mockMvc.perform(
                multipart(ROOM_BASE_URI)
                    .file(mainImage)
                    .param("name", roomCreateDto.name())
                    .param("description", roomCreateDto.description())
                    .param("price", String.valueOf(roomCreateDto.price()))
                    .param("capacity", String.valueOf(roomCreateDto.capacity()))
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Room price must be a positive number."));
    }


    /**
     * Tests the creation of a room with an invalid (zero) capacity.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateRoom_InvalidCapacity() throws Exception {
        RoomCreateDto roomCreateDto = new RoomCreateDto(
            TEST_ROOM_NAME,
            TEST_ROOM_DESCRIPTION,
            TEST_ROOM_PRICE,
            0,
            null
        );

        MockMultipartFile mainImage = new MockMultipartFile(
            "mainImage",
            "testImage.jpg",
            "image/jpeg",
            TEST_ROOM_MAIN_IMAGE
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, ROOM_BASE_URI)
                    .file(mainImage)
                    .param("name", roomCreateDto.name())
                    .param("description", roomCreateDto.description())
                    .param("price", String.valueOf(roomCreateDto.price()))
                    .param("capacity", String.valueOf(roomCreateDto.capacity()))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Room capacity must be at least 1."));
    }


    /**
     * Tests the creation of a room when the capacity is missing.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateRoom_MissingCapacity() throws Exception {
        RoomCreateDto roomCreateDto = new RoomCreateDto(TEST_ROOM_NAME, TEST_ROOM_DESCRIPTION, TEST_ROOM_PRICE, null, null);

        MockMultipartFile mainImage = new MockMultipartFile("mainImage", "testImage.jpg", "image/jpeg", TEST_ROOM_MAIN_IMAGE);

        mockMvc.perform(multipart(ROOM_BASE_URI)
                .file(mainImage)
                .param("name", roomCreateDto.name())
                .param("description", roomCreateDto.description())
                .param("price", String.valueOf(roomCreateDto.price()))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Capacity must not be null."));
    }

    /**
     * Tests the creation of a room when the description is missing.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateRoom_MissingDescription() throws Exception {
        RoomCreateDto roomCreateDto = new RoomCreateDto(TEST_ROOM_NAME, null, TEST_ROOM_PRICE, TEST_ROOM_CAPACITY, null);

        MockMultipartFile mainImage = new MockMultipartFile("mainImage", "testImage.jpg", "image/jpeg", TEST_ROOM_MAIN_IMAGE);

        mockMvc.perform(multipart(ROOM_BASE_URI)
                .file(mainImage)
                .param("name", roomCreateDto.name())
                .param("capacity", String.valueOf(roomCreateDto.capacity()))
                .param("price", String.valueOf(roomCreateDto.price()))
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Description must not be null."));
    }

    /**
     * Test case: Successfully updates an existing room.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_Success() throws Exception {
        roomRepository.save(room);

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", room.getId())
                    .param("id", String.valueOf(room.getId()))
                    .param("name", "Updated Room Name")
                    .param("description", "Updated Room Description")
                    .param("price", String.valueOf(350.0))
                    .param("capacity", String.valueOf(3))
                    .param("smartLockId", "")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Room Name"))
            .andExpect(jsonPath("$.description").value("Updated Room Description"))
            .andExpect(jsonPath("$.price").value(350.0))
            .andExpect(jsonPath("$.capacity").value(3));
    }


    /**
     * Test case: Ensures validation fails when name is too short (less than 3 characters) or empty.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_InvalidName() throws Exception {
        roomRepository.save(room);

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", room.getId())
                    .param("id", String.valueOf(room.getId()))
                    .param("name", "12")
                    .param("description", "Valid Description")
                    .param("price", String.valueOf(200.0))
                    .param("capacity", String.valueOf(2))
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Name must be between 3 and 100 characters."));
    }


    /**
     * Test case: Ensures validation fails when price is negative.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_InvalidPrice() throws Exception {
        roomRepository.save(room);

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", room.getId())
                    .param("id", String.valueOf(room.getId()))
                    .param("name", "Valid Name")
                    .param("description", "Valid Description")
                    .param("price", String.valueOf(-1.0))
                    .param("capacity", String.valueOf(2))
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Room price must be a positive number."));
    }


    /**
     * Test case: Ensures validation succeeds when capacity is at its minimum valid value (1).
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_InvalidCapacity() throws Exception {
        roomRepository.save(room);

        RoomUpdateDto invalidUpdateDto = new RoomUpdateDto(
            room.getId(),
            "Valid Name",
            "Valid Description",
            200.0,
            0,
            null
        );

        MockMultipartFile roomJson = new MockMultipartFile(
            "roomDto",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsString(invalidUpdateDto).getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", room.getId())
                    .file(roomJson)
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Room capacity must be at least 1."));
    }


    /**
     * Test case: Ensures validation succeeds when the name is at its maximum allowed length (100 characters).
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_NameWithMaxCharacters() throws Exception {
        roomRepository.save(room);

        String maxName = "a".repeat(100); // Maximum allowed length for name

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", room.getId())
                    .param("id", String.valueOf(room.getId()))
                    .param("name", maxName)
                    .param("description", "Valid Description")
                    .param("price", String.valueOf(200.0))
                    .param("capacity", String.valueOf(2))
                    .param("smartLockId", "")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk());
    }


    /**
     * Test case: Ensures validation fails when name exceeds the maximum allowed length (more than 100 characters).
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_NameExceedingMaxCharacters() throws Exception {
        roomRepository.save(room);

        String longName = "a".repeat(101);

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", room.getId())
                    .param("id", String.valueOf(room.getId()))
                    .param("name", longName)
                    .param("description", "Valid Description")
                    .param("price", "200.0")
                    .param("capacity", "2")
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Name must be between 3 and 100 characters."));
    }


    /**
     * Test case: Ensures validation succeeds when description is at its maximum allowed length (1000 characters).
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_DescriptionWithMaxCharacters() throws Exception {
        roomRepository.save(room);

        String maxDescription = "a".repeat(1000); // Maximum allowed length for description

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", room.getId())
                    .param("id", String.valueOf(room.getId()))
                    .param("name", "Valid Name")
                    .param("description", maxDescription)
                    .param("price", String.valueOf(200.0))
                    .param("capacity", String.valueOf(2))
                    .param("smartLockId", "")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk());
    }

    /**
     * Test case: Ensures validation fails when description exceeds the maximum allowed length (more than 1000 characters).
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_DescriptionExceedingMaxCharacters() throws Exception {
        roomRepository.save(room);

        String longDescription = "a".repeat(1001); // Exceeds maximum length

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", room.getId())
                    .param("id", String.valueOf(room.getId()))
                    .param("name", "Valid Name")
                    .param("description", longDescription)
                    .param("price", String.valueOf(200.0))
                    .param("capacity", String.valueOf(2))
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Description must not exceed 1000 characters."));
    }


    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_CapacityAtMinimumBoundary() throws Exception {
        roomRepository.save(room);

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", room.getId())
                    .file(new MockMultipartFile(
                        "image",
                        "example-image.jpg",
                        MediaType.IMAGE_JPEG_VALUE,
                        "DummyImageContent".getBytes()
                    ))
                    .param("id", String.valueOf(room.getId()))
                    .param("name", "Valid Name")
                    .param("description", "Valid Description")
                    .param("price", String.valueOf(200.0))
                    .param("capacity", String.valueOf(1))
                    .param("smartLockId", "")
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk());
    }


    /**
     * Test case: Ensures validation fails when capacity is set to 0.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_ZeroCapacity() throws Exception {
        roomRepository.save(room);

        RoomUpdateDto invalidUpdateDto = new RoomUpdateDto(
            room.getId(),
            "Valid Name",
            "Valid Description",
            200.0,
            0,
            null
        );

        MockMultipartFile roomJson = new MockMultipartFile(
            "roomDto",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsString(invalidUpdateDto).getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", room.getId())
                    .file(roomJson)
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Validation failed for one or more fields."))
            .andExpect(jsonPath("$.errors[0]").value("Room capacity must be at least 1."));
    }

    /**
     * Test case: Fails to update a non-existent room.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testUpdateRoom_NonExisting() throws Exception {
        RoomUpdateDto updateDto = new RoomUpdateDto(
            -999L,
            "Non-Existent Name",
            "Non-Existent Description",
            200.0,
            2,
            null
        );

        MockMultipartFile roomJson = new MockMultipartFile(
            "roomDto",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsString(updateDto).getBytes()
        );

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}", 999L) // Simulating request for a non-existing ID
                    .file(roomJson)
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isNotFound());
    }

    /**
     * Test case: Successfully creates a room with additional images
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateRoom_AdditionalImages() throws Exception {
        RoomCreateDto roomCreateDto = new RoomCreateDto(
            TEST_ROOM_NAME,
            TEST_ROOM_DESCRIPTION,
            TEST_ROOM_PRICE,
            TEST_ROOM_CAPACITY,
            null
        );

        MockMultipartFile additionalImage1 = new MockMultipartFile(
            "additionalImages",
            "image1.jpg",
            "image/jpeg",
            "Test Image Data".getBytes()
        );

        MockMultipartFile additionalImage2 = new MockMultipartFile(
            "additionalImages",
            "image2.jpg",
            "image/jpeg",
            "Test Image Data".getBytes()
        );

        MockMultipartFile mainImage = new MockMultipartFile(
            "mainImage",
            "testImage.jpg",
            "image/jpeg",
            TEST_ROOM_MAIN_IMAGE
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, ROOM_BASE_URI)
                    .file(mainImage)
                    .file(additionalImage1)
                    .file(additionalImage2)
                    .param("name", roomCreateDto.name())
                    .param("description", roomCreateDto.description())
                    .param("capacity", String.valueOf(roomCreateDto.capacity()))
                    .param("price", String.valueOf(roomCreateDto.price()))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(TEST_ROOM_NAME))
            .andExpect(jsonPath("$.description").value(TEST_ROOM_DESCRIPTION))
            .andExpect(jsonPath("$.capacity").value(TEST_ROOM_CAPACITY))
            .andExpect(jsonPath("$.price").value(TEST_ROOM_PRICE));
    }

    /**
     * Test case: Successfully creates a room with one additional image
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN", "USER"})
    public void testCreateRoom_AdditionalImage() throws Exception {
        RoomCreateDto roomCreateDto = new RoomCreateDto(
            TEST_ROOM_NAME,
            TEST_ROOM_DESCRIPTION,
            TEST_ROOM_PRICE,
            TEST_ROOM_CAPACITY,
            null
        );

        MockMultipartFile additionalImage1 = new MockMultipartFile(
            "additionalImages",
            "image1.jpg",
            "image/jpeg",
            "Test Image Data".getBytes()
        );

        MockMultipartFile mainImage = new MockMultipartFile(
            "mainImage",
            "testImage.jpg",
            "image/jpeg",
            TEST_ROOM_MAIN_IMAGE
        );

        mockMvc.perform(
                multipart(HttpMethod.POST, ROOM_BASE_URI)
                    .file(mainImage)
                    .file(additionalImage1)
                    .param("name", roomCreateDto.name())
                    .param("description", roomCreateDto.description())
                    .param("capacity", String.valueOf(roomCreateDto.capacity()))
                    .param("price", String.valueOf(roomCreateDto.price()))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value(TEST_ROOM_NAME))
            .andExpect(jsonPath("$.description").value(TEST_ROOM_DESCRIPTION))
            .andExpect(jsonPath("$.capacity").value(TEST_ROOM_CAPACITY))
            .andExpect(jsonPath("$.price").value(TEST_ROOM_PRICE));
    }

    /**
     * Test case: Successfully deletes an existing room.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testDeleteRoom_Success() throws Exception {
        roomRepository.save(room);

        mockMvc.perform(
                multipart(HttpMethod.DELETE, ROOM_BASE_URI + "/rooms/{id}", room.getId()) // Simulating request for an existing ID
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk());
    }

    /**
     * Test case: Fails to delete a non-existent room.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void testDeleteRoom_NonExisting() throws Exception {
        mockMvc.perform(
                multipart(HttpMethod.DELETE, ROOM_BASE_URI + "/rooms/{id}", -1) // Simulating request for a non-existing ID
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isNotFound());
    }

    /**
     * Test case: Successfully updates cleaning times.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void updateRoomCleaningTime_shouldUpdateCleaningTime() throws Exception {
        roomRepository.save(room);

        String from = "23:39";
        String to = "23:59";

        RoomCleaningTimeDto roomCleaningTimeDto = new RoomCleaningTimeDto(
            room.getId(),
            from,
            to
        );
        LocalDate today = LocalDate.now();

        LocalTime cleaningTimeFrom = LocalTime.parse(from);
        LocalTime cleaningTimeTo = LocalTime.parse(to);

        LocalDateTime cleaningFromDateTime = LocalDateTime.of(today, cleaningTimeFrom);
        LocalDateTime cleaningToDateTime = LocalDateTime.of(today, cleaningTimeTo);
        if(LocalDateTime.now().isBefore(cleaningFromDateTime)) {
            mockMvc.perform(
                    multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}/clean-time", room.getId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(roomCleaningTimeDto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(room.getId()))
                .andExpect(jsonPath("$.cleaningTimeFrom").value(cleaningFromDateTime + ":00"))
                .andExpect(jsonPath("$.cleaningTimeTo").value(cleaningToDateTime + ":00"));
        } else {
            mockMvc.perform(
                    multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}/clean-time", room.getId())
                        .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(roomCleaningTimeDto))
                )
                .andExpect(status().isUnprocessableEntity());
        }
    }
    /**
     * Test case: Fails to update cleaning times.
     */
    @Test
    @WithMockUser(username = ADMIN_USER, roles = {"ADMIN"})
    public void updateRoomCleaningTime_shouldNotUpdateCleaningTime() throws Exception {
        roomRepository.save(room);

        String from = "23:59";
        String to = "23:39";

        RoomCleaningTimeDto roomCleaningTimeDto = new RoomCleaningTimeDto(
            room.getId(),
            from,
            to
        );

        mockMvc.perform(
                multipart(HttpMethod.PUT, ROOM_BASE_URI + "/{id}/clean-time", room.getId())
                    .header(securityProperties.getAuthHeader(), jwtTokenizer.getAuthToken(ADMIN_USER, ADMIN_ROLES))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(roomCleaningTimeDto))
            )
            .andExpect(status().isUnprocessableEntity());
    }
}
