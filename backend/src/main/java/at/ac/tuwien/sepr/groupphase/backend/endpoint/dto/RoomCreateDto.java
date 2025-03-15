package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


/**
 * A data transfer object for creating a new room.
 *
 * <p>Contains the necessary fields for creating a room, including name, description, price,
 * capacity, and a main image. Validation annotations ensure that the provided values meet
 * the required constraints.</p>
 *
 * @param name the name of the room, must not be {@code null} and must be between 3 and 100 characters.
 * @param description the description of the room, must not be {@code null} and must be between 3 and 1000 characters.
 * @param price the price of the room, must not be {@code null} and must be non-negative.
 * @param capacity the capacity of the room, must not be {@code null} and must be at least 1.
 * @param smartLockId the ID of the smart lock of the room, can be {@code null}.
 */
public record RoomCreateDto(
    String name,

    String description,

    Double price,

    Integer capacity,

    Long smartLockId
) {
}

