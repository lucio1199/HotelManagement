package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * A data transfer object for transmitting key status data.
 * Represents the status of a door.
 *
 * @param roomId the ID of the room.
 * @param smartLockId the ID of the smart lock.
 * @param status the status of the door.
 */
public record KeyStatusDto(
    Long roomId,
    Long smartLockId,
    String status
) {
}
