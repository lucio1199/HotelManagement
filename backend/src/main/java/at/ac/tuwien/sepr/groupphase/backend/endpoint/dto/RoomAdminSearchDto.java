package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * A data transfer object for searching rooms based on various criteria.
 *
 * @param name The name of the room
 * @param maxPrice The maximum price of the room
 * @param minPrice The minimum price of the room
 * @param minCapacity The minimum capacity of the room
 * @param maxCapacity The maximum capacity of the room
 * @param description The description of the room
 */
public record RoomAdminSearchDto(
    String name,
    Double maxPrice,
    Double minPrice,
    Integer minCapacity,
    Integer maxCapacity,
    String description
) {
}
