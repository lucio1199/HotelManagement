package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * A data transfer object for updating an existing room.
 *
 * <p>Contains optional fields for updating a room's attributes. Any field that is not
 * provided (i.e., {@code null}) will not affect the corresponding attribute of the
 * room being updated.</p>
 *
 * <p>Validation annotations are used to ensure the provided values meet the required constraints.
 * For example, the name and description have character length restrictions, price and capacity
 * must be non-negative, and image fields allow for uploading new main or additional images.</p>
 *
 * @param id the id of the room to be updated, must not be null.
 * @param name the updated name of the room, must be between 3 and 100 characters if provided.
 * @param description the updated description of the room, must be below 1000 characters if provided.
 * @param price the updated price of the room, must not be negative if provided.
 * @param capacity the updated capacity of the room, must be at least 1 if provided.
 * @param smartLockId the updated ID of the smart lock of the room, can be {@code null}.
 */
public record RoomUpdateDto(
    Long id,

    String name,

    String description,

    Double price,

    Integer capacity,

    Long smartLockId
) {

}

