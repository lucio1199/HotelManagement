package at.ac.tuwien.sepr.groupphase.backend.integrationtest;

import at.ac.tuwien.sepr.groupphase.backend.config.TestSecurityConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.CheckInDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.DetailedBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.enums.Gender;
import at.ac.tuwien.sepr.groupphase.backend.enums.Nationality;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.repository.*;
import at.ac.tuwien.sepr.groupphase.backend.service.CheckInService;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.CheckInValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationUserRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.BookingRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.CheckInRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.CheckOutRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.GuestRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.RoomRepository;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Slf4j
@Import(TestSecurityConfig.class)
public class ManualCheckInEndpointTest {

    @Mock
    private CheckInRepository checkInRepository;

    @Mock
    private CheckOutRepository checkOutRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ApplicationUserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private CheckInValidator checkInValidator;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckInService checkInService;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ADMIN", "RECEPTIONIST"})
    public void givenValidRequest_whenCheckIn_thenReturnsCreated() throws Exception {
        CheckInDto checkInDto = new CheckInDto(
            1L, "John", "Doe", LocalDate.now().minusYears(18), "Vienna",
            Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789"
        );

        MockMultipartFile passport = new MockMultipartFile(
            "passport", "passport.pdf", MediaType.APPLICATION_PDF_VALUE, "PDF Content".getBytes()
        );

        MockMultipartFile checkInDtoFile = new MockMultipartFile(
            "checkInDto", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(checkInDto)
        );

        doNothing().when(checkInService).checkIn(Mockito.any(CheckInDto.class), Mockito.any(), Mockito.anyString());

        mockMvc.perform(multipart("/api/v1/manual-checkin/test@example.com")
                .file(passport)
                .file(checkInDtoFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isCreated());

        verify(checkInService, times(1)).checkIn(Mockito.any(CheckInDto.class), Mockito.any(), eq("test@example.com"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "RECEPTIONIST"})
    public void givenValidRequest_whenGetBookingById_thenReturnsBookingDetails() throws Exception {
        DetailedBookingDto bookingDto = new DetailedBookingDto(1L, 1L, 1L, LocalDate.now(),
            LocalDate.now().plusDays(7), "room", 1.0d, true, null, "BOOK-8FD8E9C0", LocalDate.parse("2025-01-13"), 2500.5 , 3, "");
        ApplicationUser user = new ApplicationUser();
        user.setEmail("test@example.com");
        user.setId(1L);

        when(checkInService.findBookingById(1L, 2L)).thenReturn(bookingDto);
        when(userService.findApplicationUserByEmail("test@example.com")).thenReturn(user);

        mockMvc.perform(get("/api/v1/manual-checkin/1/test@example.com")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(checkInService, times(1)).findBookingById(1L, 1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "RECEPTIONIST"})
    public void givenValidRequest_whenGetCheckedInStatus_thenReturnsStatusList() throws Exception {
        CheckInStatusDto[] status = {new CheckInStatusDto(1L, "Checked In")};
        when(checkInService.getCheckedInStatus("test@example.com")).thenReturn(status);

        mockMvc.perform(get("/api/v1/manual-checkin/checkin-status/test@example.com")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(checkInService, times(1)).getCheckedInStatus("test@example.com");
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "RECEPTIONIST"})
    public void givenValidRequest_whenCheckOut_thenReturnsCreated() throws Exception {
        CheckOutDto checkOutDto = new CheckOutDto(1L, "test@example.com");

        mockMvc.perform(post("/api/v1/manual-checkin/checkout")
                .content(objectMapper.writeValueAsString(checkOutDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        verify(checkInService, times(1)).checkOut(Mockito.any(CheckOutDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "RECEPTIONIST", "CLEANING_STAFF"})
    public void givenValidRequest_whenGetOccupancyStatus_thenReturnsOccupancyDetails() throws Exception {
        OccupancyDto occupancy = new OccupancyDto(1L, "Room 101");
        when(checkInService.getOccupancyStatus(1L)).thenReturn(occupancy);

        mockMvc.perform(get("/api/v1/manual-checkin/occupancy/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(checkInService, times(1)).getOccupancyStatus(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "RECEPTIONIST"})
    public void givenValidRequest_whenAddToRoom_thenReturnsCreated() throws Exception {
        // Arrange
        AddToRoomDto addToRoomDto = new AddToRoomDto(1L, "John", "Doe", LocalDate.now().minusYears(18),
            "Vienna", Gender.MALE, Nationality.AUT, "Main St. 1", "P123456", "0123456789", "test2@example.com");
        MockMultipartFile passport = new MockMultipartFile(
            "passport", "passport.pdf", MediaType.APPLICATION_PDF_VALUE, "PDF Content".getBytes()
        );

        MockMultipartFile addToRoomDtoFile = new MockMultipartFile(
            "addToRoomDto", "", MediaType.APPLICATION_JSON_VALUE,
            objectMapper.writeValueAsBytes(addToRoomDto)
        );

        when(userService.getLoggedInUserEmail()).thenReturn("test2@example.com");

        mockMvc.perform(multipart("/api/v1/manual-checkin/to-room")
                .file(passport)
                .file(addToRoomDtoFile)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isCreated());

        verify(checkInService, times(1)).addToRoom(Mockito.any(AddToRoomDto.class), Mockito.any(), eq("test2@example.com"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "RECEPTIONIST"})
    public void givenValidRequest_whenGetAllGuests_thenReturnsGuestList() throws Exception {
        GuestListDto[] guests = {new GuestListDto("John", "Doe", "test2@example.com")};
        when(checkInService.getAllGuests(1L)).thenReturn(guests);

        mockMvc.perform(get("/api/v1/manual-checkin/all-guests/1")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(checkInService, times(1)).getAllGuests(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "RECEPTIONIST"})
    public void givenValidRequest_whenRemove_thenReturnsOk() throws Exception {
        mockMvc.perform(delete("/api/v1/manual-checkin/1/test@example.com")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(checkInService, times(1)).remove(1L, "test@example.com");
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "RECEPTIONIST"})
    public void givenInvalidRoomId_whenGetOccupancyStatus_thenReturnsNotFound() throws Exception {
        when(checkInService.getOccupancyStatus(999L)).thenThrow(new NotFoundException("Room not found"));

        mockMvc.perform(get("/api/v1/manual-checkin/occupancy/999")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(checkInService, times(1)).getOccupancyStatus(999L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "RECEPTIONIST"})
    public void givenInvalidBookingId_whenCheckOut_thenReturnsNotFound() throws Exception {
        CheckOutDto checkOutDto = new CheckOutDto(999L, "test@example.com");

        doThrow(new NotFoundException("Booking with id 999 not found"))
            .when(checkInService).checkOut(Mockito.any(CheckOutDto.class));

        mockMvc.perform(post("/api/v1/manual-checkin/checkout")
                .content(objectMapper.writeValueAsString(checkOutDto))
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Booking with id 999 not found"));

        verify(checkInService, times(1)).checkOut(Mockito.any(CheckOutDto.class));
    }
}

