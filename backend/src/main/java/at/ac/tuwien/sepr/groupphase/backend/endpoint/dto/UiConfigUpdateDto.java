package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

/**
 * Data transfer object for updating the UI configuration.
 */
public record UiConfigUpdateDto(
    Long id,
    String hotelName,
    String descriptionShort,
    String description,
    String address,
    Boolean roomCleaning,
    Boolean digitalCheckIn,
    Boolean activities,
    Boolean communication,
    Boolean nuki,
    Boolean halfBoard,
    Double priceHalfBoard
) {
}