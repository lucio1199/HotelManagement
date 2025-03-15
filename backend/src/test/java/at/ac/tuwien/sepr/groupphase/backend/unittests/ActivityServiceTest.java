package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.*;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper.ActivityMapper;
import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivitySlot;
import at.ac.tuwien.sepr.groupphase.backend.entity.Room;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.impl.SimpleActivityService;
import at.ac.tuwien.sepr.groupphase.backend.service.validator.ActivityValidator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class ActivityServiceTest {

    @Mock
    private ActivityRepository activityRepository;

    @Mock
    private ActivityValidator activityValidator;

    @Mock
    private ActivityMapper activityMapper;

    @InjectMocks
    private SimpleActivityService activityService;

    @Test
    public void givenActivity_whenCreate_thenSaveAndReturnActivity() throws ValidationException, IOException {
        ActivityCreateDto activityCreateDto = new ActivityCreateDto("Arcade", "This is an arcade room", 100, 50.0, "Kids");
        Activity activity = new Activity();
        activity.setName("Arcade");
        List<ActivityTimeslotInfoDto> timeslotInfoDtos = new ArrayList<>();
        ActivityTimeslotInfoDto timeslot = new ActivityTimeslotInfoDto(1L, DayOfWeek.MONDAY, null, LocalTime.of(12,0), LocalTime.of(13,0));
        timeslotInfoDtos.add(timeslot);
        when(activityRepository.save(any(Activity.class))).thenReturn(activity);

        doNothing().when(activityValidator).validateForCreate(eq(activityCreateDto), eq(null), eq(null));

        when(activityMapper.activityCreateDtoToActivity(eq(activityCreateDto), eq(null), eq(null), eq(timeslotInfoDtos))).thenReturn(activity);
        when(activityMapper.activityToDetailedActivityDto(eq(activity))).thenReturn(new DetailedActivityDto(
            1L, "Arcade", "This is an arcade room", 50.0, 100, null, new ArrayList<>(), timeslotInfoDtos, "Kids"));

        DetailedActivityDto savedActivity = activityService.create(activityCreateDto, null, null, timeslotInfoDtos);

        verify(activityRepository, times(1)).save(any(Activity.class));
        assertEquals("Arcade", savedActivity.name());
    }

    @Test
    public void givenValidActivityId_whenFindOne_thenReturnDetailedActivityDto() {
        Activity activity = new Activity();
        activity.setId(1L);
        activity.setName("Yoga Class");

        List<ActivityTimeslotInfoDto> timeslotInfoDtos = new ArrayList<>();
        ActivityTimeslotInfoDto timeslot = new ActivityTimeslotInfoDto(1L, DayOfWeek.MONDAY, null, LocalTime.of(12,0), LocalTime.of(13,0));
        timeslotInfoDtos.add(timeslot);

        DetailedActivityDto detailedActivityDto = new DetailedActivityDto(
            1L, "Yoga Class", "Relaxing yoga session", 30.0, 20, null, null, timeslotInfoDtos, "Wellness"
        );

        when(activityRepository.findActivityById(1L)).thenReturn(Optional.of(activity));
        when(activityMapper.activityToDetailedActivityDto(activity)).thenReturn(detailedActivityDto);

        DetailedActivityDto result = activityService.findOne(1L);

        assertEquals("Yoga Class", result.name());
        verify(activityRepository, times(1)).findActivityById(1L);
    }

    @Test
    public void givenInvalidId_whenFindOne_thenThrowNotFoundException() {
        when(activityRepository.findActivityById(anyLong())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> activityService.findOne(999L));

        assertEquals("Activity not found with ID: 999", exception.getMessage());
        verify(activityRepository, times(1)).findActivityById(999L);
    }

    @Test
    public void givenActivityUpdateDto_whenUpdate_thenUpdateAndReturnActivity() throws ValidationException, IOException {
        Activity existingActivity = new Activity();
        existingActivity.setId(1L);
        existingActivity.setName("Yoga");

        existingActivity.setAdditionalImages(new ArrayList<>());

        List<ActivityTimeslotInfoDto> timeslotInfoDtos = new ArrayList<>();
        ActivityTimeslotInfoDto timeslot = new ActivityTimeslotInfoDto(1L, DayOfWeek.MONDAY, null, LocalTime.of(12,0), LocalTime.of(13,0));
        timeslotInfoDtos.add(timeslot);

        ActivityUpdateDto updateDto = new ActivityUpdateDto(1L, "Arcade", "Arcade Room", 40.0, 25, "Kids");

        Activity updatedActivity = new Activity();
        updatedActivity.setId(1L);
        updatedActivity.setName("Arcade");
        updatedActivity.setAdditionalImages(new ArrayList<>());

        DetailedActivityDto detailedActivityDto = new DetailedActivityDto(
            1L, "Arcade", "Arcade Room", 40.0, 25, null, null, timeslotInfoDtos, "Kids"
        );

        when(activityRepository.findById(1L)).thenReturn(Optional.of(existingActivity));
        when(activityRepository.save(any(Activity.class))).thenReturn(updatedActivity);
        when(activityMapper.activityToDetailedActivityDto(updatedActivity)).thenReturn(detailedActivityDto);

        DetailedActivityDto result = activityService.update(1L, updateDto, null, null, timeslotInfoDtos);

        assertEquals("Arcade", result.name());
        verify(activityRepository, times(1)).save(any(Activity.class));
    }

    @Test
    @Disabled
    public void testGetPaginatedTimeslots_Success() {
        Long activityId = 1L;
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "date"));
        List<ActivitySlot> slots = List.of(
            new ActivitySlot(1L, null, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0), 10, 5),
            new ActivitySlot(2L, null, LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0), 8, 3)
        );
        Page<ActivitySlot> slotsPage = new PageImpl<>(slots);

        when(activityRepository.findTimeslotsByActivityId(activityId, LocalDate.now(), LocalTime.now(), pageRequest)).thenReturn(slotsPage);
        when(activityMapper.activitySlotPageToDtoPage(slotsPage)).thenReturn(new PageImpl<>(
            List.of(
                new ActivitySlotDto(1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0), 10, 5),
                new ActivitySlotDto(2L, LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(16, 0), 8, 3)
            )
        ));

        Page<ActivitySlotDto> result = activityService.getPaginatedTimeslots(activityId, pageRequest);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(activityRepository, times(1)).findTimeslotsByActivityId(activityId, LocalDate.now(), LocalTime.now(), pageRequest);
        verify(activityMapper, times(1)).activitySlotPageToDtoPage(slotsPage);
    }

    @Test
    @Disabled
    public void testGetFilteredTimeslots_Success() throws ValidationException {
        Long activityId = 1L;
        ActivitySlotSearchDto searchDto = new ActivitySlotSearchDto(LocalDate.now(), 2);
        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "date"));
        List<ActivitySlot> filteredSlots = List.of(
            new ActivitySlot(1L, null, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0), 10, 5)
        );
        Page<ActivitySlot> filteredSlotsPage = new PageImpl<>(filteredSlots);

        when(activityRepository.findFilteredTimeslots(
            activityId, searchDto.date(), searchDto.participants(),  LocalDate.now(), LocalTime.now(), pageRequest))
            .thenReturn(filteredSlotsPage);
        when(activityMapper.activitySlotPageToDtoPage(filteredSlotsPage)).thenReturn(new PageImpl<>(
            List.of(new ActivitySlotDto(1L, LocalDate.now(), LocalTime.of(10, 0), LocalTime.of(12, 0), 10, 5))
        ));

        Page<ActivitySlotDto> result = activityService.getFilteredTimeslots(activityId, searchDto, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(activityRepository, times(1)).findFilteredTimeslots(activityId, searchDto.date(), searchDto.participants(), LocalDate.now(), LocalTime.now(), pageRequest);
        verify(activityValidator, times(1)).validateForTimeslotsFilter(searchDto, activityId);
        verify(activityMapper, times(1)).activitySlotPageToDtoPage(filteredSlotsPage);
    }
}
