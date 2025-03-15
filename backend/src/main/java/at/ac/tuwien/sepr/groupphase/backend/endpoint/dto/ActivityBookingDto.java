package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ActivityBookingDto(
    Long id,
    Long activityId,
    String activityName,
    LocalDate bookingDate,
    LocalTime startTime,
    LocalTime endTime,
    LocalDate date,
    Integer participants,
    Boolean paid
) {
}
