package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import java.time.LocalDate;

/**
 * A data transfer object for searching rooms based on various criteria.
 *
 * <p>Provides fields for filtering rooms by name, price range, capacity range,
 * and description. Each field is optional, and a combination of these criteria
 * can be used to narrow down the search results.</p>
 *
 * @param maxPrice the maximum price of the room, used to filter rooms that are less than or equal to this value.
 * @param minPrice the minimum price of the room, used to filter rooms that are greater than or equal to this value.
 * @param capacity the capacity of the room, used to filter rooms that can accommodate at least this many people.
 */
public record RoomSearchDto(
    LocalDate startDate,
    LocalDate endDate,
    Double maxPrice,
    Double minPrice,
    Integer capacity
) {
}
