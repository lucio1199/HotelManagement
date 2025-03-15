package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDateTime;

public record RoomCleaningTimeDto(
    Long id,
    String cleaningTimeFrom,
    String cleaningTimeTo
) {
}
