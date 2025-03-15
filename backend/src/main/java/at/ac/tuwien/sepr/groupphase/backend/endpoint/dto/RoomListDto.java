package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.persistence.Column;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

/**
 * A data transfer object representing a summarized view of a room for listing purposes.
 *
 * <p>Contains essential fields to display a room in a list, such as its ID, name, price,
 * capacity, and main image.</p>
 *
 * @param id the unique identifier of the room.
 * @param name the name of the room.
 * @param price the price of the room.
 * @param capacity the capacity of the room, representing the number of people it can accommodate.
 * @param mainImage the URL or identifier for the main image of the room.
 * @param lastCleanedAt the DateTime of the last cleaning.
 */
public record RoomListDto(
    Long id,
    String name,
    double price,
    int capacity,
    LocalDateTime lastCleanedAt,
    LocalDateTime cleaningTimeFrom,
    LocalDateTime cleaningTimeTo,
    String mainImage
) {}

