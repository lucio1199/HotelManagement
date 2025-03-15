package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * A data transfer object for transmitting occupancy status data.
 * Represents the status of a room.
 *
 * @param roomId the ID of the room.
 * @param status the status of the room.
 */
public record OccupancyDto(
    Long roomId,
    String status
) {
}
