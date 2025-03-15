package at.ac.tuwien.sepr.groupphase.backend.endpoint.mapper;

import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.ActivityBookingDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.Activity;
import at.ac.tuwien.sepr.groupphase.backend.entity.ActivityBooking;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ActivityBookingMapper {

    default ActivityBookingDto activityBookingToActivityBookingDto(ActivityBooking activityBooking, Activity activity) {
        return new ActivityBookingDto(
            activityBooking.getId(),
            activity.getId(),
            activity.getName(),
            activityBooking.getBookingDate(),
            activityBooking.getActivitySlot().getStartTime(),
            activityBooking.getActivitySlot().getEndTime(),
            activityBooking.getActivitySlot().getDate(),
            activityBooking.getParticipants(),
            activityBooking.isPaid()
        );
    }

    default List<ActivityBookingDto> activityBookingsToActivityBookingDtos(List<ActivityBooking> activityBookings) {
        if (activityBookings == null) {
            return null;
        }

        return activityBookings.stream()
            .map(activityBooking -> activityBookingToActivityBookingDto(activityBooking, activityBooking.getActivitySlot().getActivity()))
            .collect(Collectors.toList());
    }
}
