package at.ac.tuwien.sepr.groupphase.backend.service.validator;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingCreateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivitySlot;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivityRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ActivitySlotRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.ApplicationUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

@Component
@Slf4j
public class ActivityBookingValidator {

    private final ActivityRepository activityRepository;
    private final ActivitySlotRepository activitySlotRepository;
    private final ApplicationUserRepository userRepository;

    public ActivityBookingValidator(ActivityRepository activityRepository, ActivitySlotRepository activitySlotRepository, ApplicationUserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.activitySlotRepository = activitySlotRepository;
        this.userRepository = userRepository;
    }

    public void validateForCreate(ActivityBookingCreateDto bookingCreateDto, Activity activity, ActivitySlot activitySlot) throws ValidationException {

        ArrayList<String> errors = new ArrayList<>();

        if (activitySlot.getActivity().getId() != activity.getId()) {
            throw new IllegalArgumentException("Activity slot does not belong to activity");
        }

        if (activitySlot.getCapacity() - activitySlot.getOccupied() < bookingCreateDto.participants()) {
            errors.add("Not enough capacity in activity slot");
        }

        LocalTime slotStartTime = activitySlot.getStartTime();
        LocalDateTime slotStartDateTime = LocalDateTime.of(activitySlot.getDate(), slotStartTime);
        LocalDateTime nowDateTime = LocalDateTime.now();
        if (Duration.between(nowDateTime, slotStartDateTime).toHours() < 2) {
            errors.add("Activity slot must start at least 2 hours from now");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed for one or more fields.", errors);
        }


    }
}