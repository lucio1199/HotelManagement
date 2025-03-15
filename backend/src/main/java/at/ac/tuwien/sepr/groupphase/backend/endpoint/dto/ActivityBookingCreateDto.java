package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;

public record ActivityBookingCreateDto(
    Long activityId,
    Long activitySlotId,
    LocalDate bookingDate,
    Integer participants,
    String userEmail
) {
}
