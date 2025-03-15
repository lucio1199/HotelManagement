package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * A data transfer object representing a timeslot for an activity.
 *
 * <p>Contains information about the timeslot of an activity, including the day of the week,
 * a specific date, start time, and end time. Each timeslot is identified by a unique ID.</p>
 *
 * @param id the unique identifier of the timeslot, may be {@code null} if not yet persisted.
 * @param dayOfWeek the day of the week for recurring activities, may be {@code null} if not applicable.
 * @param specificDate a specific date for the activity, may be {@code null} if the timeslot is recurring.
 * @param startTime the start time of the timeslot, must not be {@code null}.
 * @param endTime the end time of the timeslot, must not be {@code null}.
 */
public record ActivityTimeslotInfoDto(
    Long id,
    DayOfWeek dayOfWeek,
    LocalDate specificDate,
    LocalTime startTime,
    LocalTime endTime
) {
}
