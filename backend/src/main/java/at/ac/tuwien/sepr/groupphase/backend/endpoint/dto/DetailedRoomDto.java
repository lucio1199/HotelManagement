package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A detailed data transfer object representing a room with comprehensive information.
 *
 * <p>Includes all details about a room, such as its ID, name, description, price,
 * capacity, main image, and a list of additional images. Validation annotations
 * ensure that the fields meet the required constraints.</p>
 *
 * @param id the unique identifier of the room, must not be {@code null}.
 * @param name the name of the room, must not be {@code null} and must be between 3 and 100 characters.
 * @param description the description of the room, must not be {@code null} and must be between 3 and 1000 characters.
 * @param price the price of the room, must not be {@code null} and must be non-negative.
 * @param capacity the capacity of the room, must not be {@code null} and must be at least 1.
 * @param mainImage the URL or identifier for the main image of the room, must not be {@code null}.
 * @param additionalImages a list of additional image URLs or identifiers, must contain at least one image.
*  @param smartLockId the ID of the smart lock of the room, can be {@code null}.
 */
public record DetailedRoomDto(
    Long id,

    String name,

    String description,

    Double price,

    Integer capacity,

    String mainImage,

    List<String> additionalImages,

    Long smartLockId
) {}