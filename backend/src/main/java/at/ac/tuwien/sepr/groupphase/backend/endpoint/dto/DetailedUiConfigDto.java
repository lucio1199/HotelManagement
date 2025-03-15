package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;


import java.util.List;

/**
 * A DTO for the detailed UI configuration.
 */
public record DetailedUiConfigDto(
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
    Double priceHalfBoard,
    List<String> images
) {
}
