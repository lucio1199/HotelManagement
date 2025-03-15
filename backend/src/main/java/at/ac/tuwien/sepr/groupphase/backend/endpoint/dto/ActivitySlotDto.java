package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ActivitySlotDto(
    Long id,
    LocalDate date,
    LocalTime startTime,
    LocalTime endTime,
    int capacity,
    int occupied
) {


}
